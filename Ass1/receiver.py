#!/usr/bin/env python3
# -*- coding: utf-8 -*-

###
#  Overview
#  --------
#  
#  The scenario is simple: a sender, or multiple senders, send a sequence of 
#  random numbers to a receiver. The receiver performs some basic modulo 
#  arithmetic to determine whether each random number it receives is odd or
#  even, and sends this information back to the sender.
#  
#  Message format from sender to receiver (2 bytes total):
#  
#  +-------------+
#  | Byte Offset |
#  +------+------+
#  |   0  |   1  |
#  +------+------+
#  |    number   |
#  +-------------+
#  
#  Message format from receiver to sender (3 bytes total):
#  
#  +--------------------+
#  |    Byte Offset     |
#  +------+------+------+
#  |   0  |   1  |   2  |
#  +------+------+------+
#  |    number   |  odd |
#  +-------------+------+
#  
#  
#  Description
#  -----------
#  
#  - The sender is invoked with three command-line arguments:  
#      1. the hostname or IP address of the receiver  
#      2. the port number of the receiver
#      3. the duration to run, in seconds, before terminating
#  
#  - The receiver is invoked with two command-line arguments:
#      1. the port number on which to listen for incoming messages
#      2. the duration to wait for a message, in seconds, before terminating
#  
#  The sender will spawn two child threads: one to listen for responses from
#  the receiver, and another to wait for a timer to expire. Meanwhile, the 
#  main thread will sit in a loop and send a sequence of random 16-bit 
#  unsigned integers to the receiver. Messages will be sent and received 
#  through an ephemeral (OS allocated) port. After each message is sent, the 
#  sender may sleep for a random amount of time.  Once the timer expires, 
#  the child threads, and then the sender process, will gracefully terminate.
#  
#  The receiver is single threaded and sits in a loop, waiting for messages. 
#  Each message is expected to contain a 16-bit unsigned integer. The receiver 
#  will determine whether the number is odd or even, and send a message back 
#  with the original number as well as a flag indicating whether the number 
#  is odd or even. If no message is received within a certain amount of time, 
#  the receiver will terminate.
#  
#  
#  Features
#  --------
#  
#  - Parsing command-line arguments
#  - Random number generation (sender only)
#  - Modulo arithmetic (receiver only)
#  - Communication via UDP sockets
#  - Non-blocking sockets (sender only)
#  - Blocking sockets with a timeout (receiver only)
#  - Using a "connected" UDP socket, to send() and recv() (sender only)
#  - Using an "unconnected" UDP socket, to sendto() and recvfrom() (receiver 
#    only)
#  - Conversion between host byte order and network byte order for 
#    multi-byte fields.
#  - Timers (sender only)
#  - Multi-threading (sender only)
#  - Simple logging
#  
#  
#  Usage
#  -----
#  
#  1. Run the receiver program:
#  
#      $ python3 receiver.py 54321 10
#  
#     This will invoke the receiver to listen on port 54321 and terminate
#     if no message is receieved within 10 seconds.
#  
#  2. Run the sender program:
#  
#      $ python3 sender.py 127.0.0.1 54321 30
#  
#     This will invoke the sender to send a sequence of random numbers to
#     the receiver at 127.0.0.1:54321, and terminate after 30 seconds.
#  
#     Multiple instances of the sender can be run against the same receiver.
#  
#  
#  Notes
#  -----
#  
#  - The sender and receiver are designed to be run on the same machine, 
#    or on different machines on the same network. They are not designed 
#    to be run on different networks, or on the public internet.
#  
#  
#  Author
#  ------
#  
#  Written by Tim Arney (t.arney@unsw.edu.au) for COMP3331/9331.
#  
#  
#  CAUTION
#  -------
#  
#  - This code is not intended to be simply copy and pasted.  Ensure you 
#    understand this code before using it in your own projects, and adapt
#    it to your own requirements.
#  - The sender adds artificial delay to its sending thread.  This is purely 
#    for demonstration purposes.  In general, you should NOT add artificial
#    delay as this will reduce efficiency and potentially mask other issues.
###

import socket
import sys, os
import time
import random

class STP_segment:
    def __init__(self, STP_type: int, STP_seqno: int, STP_data: bytes = b''):
        self.STP_type = STP_type  # Type of the segment (DATA = 0, ACK = 1, SYN = 2, FIN = 3)
        self.STP_seqno = STP_seqno  # Sequence number of the segment
        self.STP_data = STP_data  # Data contained in the segment (default is empty)

    def to_bytes(self) -> bytes:
        """Converts the segment to bytes."""
        if self.STP_type == 0:  # DATA segment
            header = self.STP_type.to_bytes(2, 'big') + self.STP_seqno.to_bytes(2, 'big')
            return header + self.STP_data
        else:  # Other segment types (ACK, SYN, FIN)
            return self.STP_type.to_bytes(2, 'big') + self.STP_seqno.to_bytes(2, 'big')

    @classmethod
    def from_bytes(cls, segment_bytes: bytes) -> 'STP_segment':
        """Creates an STP_segment object from bytes."""
        STP_type = int.from_bytes(segment_bytes[:2], 'big')
        STP_seqno = int.from_bytes(segment_bytes[2:4], 'big')
        STP_data = segment_bytes[4:]
        return cls(STP_type, STP_seqno, STP_data)

    def get_type(self):
        return self.STP_type
    def get_seqno(self):
        return self.STP_seqno
    def get_data(self):
        return self.STP_data

NUM_ARGS = 4  # Number of command-line arguments
SEGMENT_SIZE = 1004
BUF_SIZE = 3  # Size of buffer for sending/receiving data
LOG_FILE = "receiver_log.txt"
MAX_SLEEP = 2 

def parse_wait_time(wait_time_str, min_wait_time=1, max_wait_time=60):
    """Parse the wait_time argument from the command-line.

    The parse_wait_time() function will attempt to parse the wait_time argument
    from the command-line into an integer. If the wait_time argument is not 
    numerical, or within the range of acceptable wait times, the program will
    terminate with an error message.

    Args:
        wait_time_str (str): The wait_time argument from the command-line.
        min_wait_time (int, optional): Minimum acceptable wait time. Defaults to 1.
        max_wait_time (int, optional): Maximum acceptable wait time. Defaults to 60.

    Returns:
        int: The wait_time as an integer.
    """
    try:
        wait_time = int(wait_time_str)
    except ValueError:
        sys.exit(f"Invalid wait_time argument, must be numerical: {wait_time_str}")
    
    if not (min_wait_time <= wait_time <= max_wait_time):
        sys.exit(f"Invalid wait_time argument, must be between {min_wait_time} and {max_wait_time} seconds: {wait_time_str}")
                 
    return wait_time

def parse_port(port_str, min_port=49152, max_port=65535):
    """Parse the port argument from the command-line.

    The parse_port() function will attempt to parse the port argument
    from the command-line into an integer. If the port argument is not 
    numerical, or within the acceptable port number range, the program will
    terminate with an error message.

    Args:
        port_str (str): The port argument from the command-line.
        min_port (int, optional): Minimum acceptable port. Defaults to 49152.
        max_port (int, optional): Maximum acceptable port. Defaults to 65535.

    Returns:
        int: The port as an integer.
    """
    try:
        port = int(port_str)
    except ValueError:
        sys.exit(f"Invalid port argument, must be numerical: {port_str}")
    
    if not (min_port <= port <= max_port):
        sys.exit(f"Invalid port argument, must be between {min_port} and {max_port}: {port}")
                 
    return port

def parse_win(curr_win):
    try:
        curr_win = int(curr_win)
    except ValueError:
        sys.exit(f"Invalid max_win argument, must be numerical: {curr_win}")
    if curr_win % 1000 != 0:
        sys.exit(f"Invalid max_win argument, must be a multiple of 1000 bytes")
    return curr_win

def parse_file(curr_file):
    if not os.access(curr_file, os.R_OK):
        sys.exit(f"Invalid input_file argument, must be an exist file")
    return curr_file

def recvISN_sendACK(s, receiver_port, sender_port):
    
    try:
        segment_bytes, sender_addr = s.recvfrom(SEGMENT_SIZE)
    except socket.timeout:
        print(f"No data within 5 seconds, shutting down.")

    if len(segment_bytes) < 3:
        print(f"recvfrom: received short message: {segment_bytes}", file=sys.stderr)
    start_time = round(time.time()*1000, 2)
    segment = STP_segment.from_bytes(segment_bytes)
    num = segment.get_seqno()
    
    log_entry = f"rcv 0.00 SYN {num} 0"
    with open(LOG_FILE, "a") as log_file:
        log_file.write(log_entry + "\n")
        print(log_entry)

    num += 1
    segment = STP_segment(STP_type=1, STP_seqno=num%(2**16), STP_data=b'')
    net_num = segment.to_bytes()
   
    if (s.sendto(net_num, ((sender_addr[0], sender_port))) != len(net_num)):
        print(f"sendto: partial/failed send, message: {num}", file=sys.stderr)
    
    curr_time = round(time.time()*1000, 2)
    interval_time = curr_time - start_time
    log_entry = f"snd {interval_time:.2f} ACK {num} 0"
    with open(LOG_FILE, "a") as log_file:
        log_file.write(log_entry + "\n")
        print(log_entry)
    #time.sleep(random.uniform(0, MAX_SLEEP + 1))
    return num, start_time

if __name__ == "__main__":

    if len(sys.argv) != NUM_ARGS + 1:
        sys.exit(f"Usage: {sys.argv[0]} sender_port receiver_port txt_file_to_send max_win")

    receiver_port = parse_port(sys.argv[1])
    #wait_time = parse_wait_time(sys.argv[2])
    sender_port = parse_port(sys.argv[2])

    txt_file_received = sys.argv[3]
    max_win = parse_win(sys.argv[4])
    if os.path.isfile(txt_file_received):
        os.remove(txt_file_received)

    if os.path.isfile(LOG_FILE):
        os.remove(LOG_FILE)
    total_data = 0
    dict_seqno = {}
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.bind(('', receiver_port))              # bind to `port` on all interfaces
        s.settimeout(5)  # 设置超时时间为5秒
        #s.settimeout(wait_time)         # set timeout for receiving data
        curr_ack, start_time = recvISN_sendACK(s, receiver_port, sender_port)

        with open(txt_file_received, 'a') as file:
            while True:
                # Here we're using recvfrom() and sendto(), but we could also 
                # connect() this UDP socket to set communication with a particular 
                # peer. This would allow us to use send() and recv() instead, 
                # but only communicate with one peer at a time.
                '''
                
                '''
                try:
                    buf, sender_addr = s.recvfrom(SEGMENT_SIZE)
                    segment = STP_segment.from_bytes(buf)

                    data = segment.get_data().decode()

                except socket.timeout:
                    print(f"No data within 5 seconds, shutting down.")
                    break

                # Packet was received, first (and only) field is multi-byte, 
                # so need to convert from network byte order (big-endian) to 
                # host byte order.  Then log the recv.

                sender_addr = ((sender_addr[0], sender_port))
                
                # Determine whether the number is odd or even, and append the 
                # result (as a single byte) to the buffer.
                buf = STP_segment.from_bytes(buf)
                curr_ack = buf.get_seqno()
                curr_data = buf.get_data()
                curr_type = buf.get_type()
                if curr_type == 3:
                    curr_time = round(time.time()*1000, 2)
                    interval_time = curr_time - start_time
                    log_entry = f"rcv {interval_time:.2f} FIN {curr_ack} 0"
                    with open(LOG_FILE, "a") as log_file:
                        log_file.write(log_entry + "\n")
                        print(log_entry)
                    file.close()
                    curr_ack += 1
                    segment = STP_segment(STP_type=1, STP_seqno=curr_ack%(2**16), STP_data=b'')
                    net_num = segment.to_bytes()
                    curr_time = round(time.time()*1000, 2)
                    interval_time = curr_time - start_time
                    log_entry = f"snd {interval_time:.2f} ACK {curr_ack} 0"
                    with open(LOG_FILE, "a") as log_file:
                        log_file.write(log_entry + "\n")

                    if (s.sendto(net_num, ((sender_addr[0], sender_port))) != len(net_num)):
                        print(f"sendto: partial/failed send, message: {curr_ack}", file=sys.stderr)
                    break
                else:
                    if curr_ack not in dict_seqno:
                        dict_seqno[curr_ack] = 1
                        total_data += len(curr_data)
                        file.write(data)
                    else:
                        dict_seqno[curr_ack] += 1
                
                curr_time = round(time.time()*1000, 2)
                interval_time = curr_time - start_time
                log_entry = f"rcv {interval_time:.2f} DATA {curr_ack} {len(curr_data)}"
                with open(LOG_FILE, "a") as log_file:
                    log_file.write(log_entry + "\n")
                    print(log_entry)

                
                curr_ack += len(curr_data)
                segment = STP_segment(STP_type=1, STP_seqno=curr_ack%(2**16), STP_data=b'')
                net_num = segment.to_bytes()
                if (s.sendto(net_num, ((sender_addr[0], sender_port))) != len(net_num)):
                        print(f"sendto: partial/failed send, message: {curr_ack}", file=sys.stderr)
                #time.sleep(random.uniform(0, MAX_SLEEP + 1))
                # Log the send and send the reply.
                curr_time = round(time.time()*1000, 2)
                interval_time = curr_time - start_time
                log_entry = f"snd {interval_time:.2f} ACK {curr_ack} 0"
                with open(LOG_FILE, "a") as log_file:
                    log_file.write(log_entry + "\n")
                    print(log_entry)
                s.settimeout(5)
        duplicate_rev = 0
        for i in dict_seqno:
            duplicate_rev += (dict_seqno[i] - 1)
        log_entry = f"Original data received: {total_data}\nOriginal segments received: {len(dict_seqno)}\nDup data segments received: {duplicate_rev}\nDup ack segments sent: {duplicate_rev}"
        with open(LOG_FILE, "a") as log_file:
            log_file.write('\n' + log_entry + "\n")
            print('\n' + log_entry)
        file.close()
    sys.exit(0)