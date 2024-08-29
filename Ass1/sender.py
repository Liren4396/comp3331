#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import random
import socket
import sys
import threading
import time
from dataclasses import dataclass
import os
from collections import namedtuple
import queue

received_seqno_queue = queue.Queue()
lost_data = queue.Queue()
received_data = queue.Queue()
recv_data_drop = 0
dict_seqno = queue.Queue()
ack_lost_data = queue.Queue()

NUM_ARGS  = 7  # Number of command-line arguments
BUF_SIZE  = 3  # Size of buffer for receiving messages
MAX_SLEEP = 2  # Max seconds to sleep before sending the next message
SEGMENT_SIZE = 1000
LOG_FILE = "sender_log.txt"
'''
STP_type
data = 0
ack = 1
syn = 2
fin = 3
'''
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
@dataclass
class Control:
    """Control block: parameters for the sender program."""
    host: str               # Hostname or IP address of the receiver
    port: int               # Port number of the receiver
    socket: socket.socket   # Socket for sending/receiving messages
    run_time: int           # Run time in seconds
    is_alive: bool = True   # Flag to signal the sender program to terminate

def parse_run_time(run_time_str, min_run_time=1, max_run_time=1000):
    """Parse the run_time argument from the command-line.

    The parse_run_time() function will attempt to parse the run_time argument
    from the command-line into an integer. If the run_time argument is not 
    numerical, or within the range of acceptable run times, the program will
    terminate with an error message.

    Args:
        run_time_str (str): The run_time argument from the command-line.
        min_run_time (int, optional): Minimum acceptable run time. Defaults to 1.
        max_run_time (int, optional): Maximum acceptable run time. Defaults to 60.

    Returns:
        int: The run_time as an integer.
    """
    try:
        run_time = int(run_time_str)
    except ValueError:
        sys.exit(f"Invalid run_time argument, must be numerical: {run_time_str}")
    
    if not (min_run_time <= run_time <= max_run_time):
        sys.exit(f"Invalid run_time argument, must be between {min_run_time} and {max_run_time} seconds: {run_time_str}")
                 
    return run_time

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

def setup_socket(host, port):
    """Setup a UDP socket for sending messages and receiving replies.

    The setup_socket() function will setup a UDP socket for sending data and
    receiving replies. The socket will be associated with the peer address 
    given by the host:port arguments. This will allow for send() calls without
    having to specify the peer address each time. It will also limit the 
    datagrams received to only those from the peer address.  The socket will
    be set to non-blocking mode.  If the socket fails to connect the program 
    will terminate with an error message.

    Args:
        host (str): The hostname or IP address of the receiver.
        port (int): The port number of the receiver.

    Returns:
        socket: The newly created socket.
    """
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # UDP sockets are "connection-less", but we can still connect() to
    # set the socket's peer address.  The peer address identifies where 
    # all datagrams are sent on subsequent send() calls, and limits the
    # remote sender for subsequent recv() calls.
    try:
        sock.connect((host, port))
    except Exception as e:
        sys.exit(f"Failed to connect to {host}:{port}: {e}")

    # Set socket timeout to 0, this is equivalent to setting it as non-blocking:
    # sock.setblocking(False)
    sock.settimeout(0)  
    sock.setblocking(False)
    return sock

def recv_thread(control, s, start_time, flp, lost_data, rto, total_data_sent, retransmitted_seg):
    """The receiver thre ad function.

    The recv_thread() function is the entry point for the receiver thread. It
    will sit in a loop, checking for messages from the receiver. When a message 
    is received, the sender will unpack the message and print it to the log. On
    each iteration of the loop, it will check the `is_alive` flag. If the flag
    is false, the thread will terminate. The `is_alive` flag is shared with the
    main thread and the timer thread.

    Args:
        control (Control): The control block for the sender program.
    """
   
    global dict_seqno
    while control.is_alive:
        try:
            '''
            if lost_data.empty():
                control.is_alive = False
                break
            '''
            #buf, receiver_addr = s.recvfrom(SEGMENT_SIZE)
            buf, receiver_addr = s.recvfrom(SEGMENT_SIZE)
            #s.settimeout(5)  # 设置超时时间为5秒
            if len(buf) < BUF_SIZE - 1:
                print(f"recv: received short message of {buf} bytes", file=sys.stderr)
                continue    # Short message, ignore it
            # Convert first 2 bytes (i.e. the number) from network byte order 
            # (big-endian) to host byte order, and extract the `odd` flag.
            # odd = buf[2]
            segment = STP_segment.from_bytes(buf)
            
            num = segment.get_seqno()
            curr_time = round(time.time()*1000, 2)
            interval_time = curr_time - start_time

            if random.random() < rlp:
                global recv_data_drop
                recv_data_drop += 1

                log_entry = f"drp {interval_time:.2f} ACK {num} 0"
                with open(LOG_FILE, "a") as log_file:
                    log_file.write(log_entry + "\n")
                    print(log_entry)
                while not ack_lost_data.empty():
                    ack, chunk = ack_lost_data.get()
                    if ack == num-1000:
                        lost_data.put([ack, chunk])
                        #retransmitted = threading.Timer(rto / 1000, retransmitted_thread, args=(receiver_control, start_time, flp, lost_data, rto, total_data, retransmitted_seg))
                        #retransmitted.start()
                        #retransmitted.join()
                        break
                
            else:
                # Log the received message
                log_entry = f"rcv {interval_time:.2f} ACK {num} 0"
                with open(LOG_FILE, "a") as log_file:
                    log_file.write(log_entry + "\n")
                    print(log_entry)
                received_seqno_queue.put(num)
                dict_seqno.put(num)
                
            # Log the received message
            # print(f"{receiver_addr[0]}:{receiver_addr[1]}: rcv: {num:>5} ack")
            # print(f"{control.host}:{control.port}: rcv: {num:>5} ack")
        except BlockingIOError:
            continue    # No data available to read
        except ConnectionRefusedError:
            print(f"recv: connection refused by {control.host}:{control.port}, shutting down...", file=sys.stderr)
            control.is_alive = False
            break
        except socket.timeout:
            print(f"No data within 5 seconds, shutting down.")
            break
    
def timer_thread(control, s):
    """Stop execution when the timer expires.

    The timer_thread() function will be called when the timer expires. It will
    print a message to the log, and set the `is_alive` flag to False. This will
    signal the receiver thread, and the sender program, to terminate.

    Args:
        control (Control): The control block for the sender program.
    """
    control.is_alive = False
    #s.settimeout(0)

def retransmitted_thread(control, start_time, flp, lost_data, rto, total_data, retransmitted_seg):
    if not lost_data.empty() and control.is_alive == True:

        lost_ACK, lost_chunk = lost_data.get()
        if lost_ACK == -1 and lost_chunk == -1:
            lost_ACK, lost_chunk = lost_data.get()
        send_data(control, lost_chunk, lost_ACK, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)


def parse_win(curr_win):
    try:
        curr_win = int(curr_win)
        
    except ValueError:
        sys.exit(f"Invalid max_win argument, must be numerical: {curr_win}")
    if curr_win % 1000 != 0:
        sys.exit(f"Invalid max_win argument, must be a multiple of 1000 bytes")
    return curr_win

def parse_loss_probability(curr_lp):
    try:
        curr_lp = float(curr_lp)
    except ValueError:
        sys.exit(f"Invalid lp argument, must be numerical: {curr_lp}")
    if curr_lp > 1 or curr_lp < 0:
        sys.exit(f"Invalid lp argument, must be less than 0 or greater than 1")
    return curr_lp

def parse_file(curr_file):
    if not os.access(curr_file, os.R_OK):
        sys.exit(f"Invalid file argument, file must be readable")
    return curr_file

def send_isn(receiver_control, host, port, num):
    start_time = round(time.time()*1000, 2)
    
    
    if random.random() < flp:
        log_entry = f"drp 0.00 SYN {num} 0"
        with open(LOG_FILE, "a") as log_file:
            log_file.write(log_entry + "\n")
            print(log_entry)
        return start_time, -1
    else:
        segment = STP_segment(STP_type=2, STP_seqno=num, STP_data=b'')
        net_num = segment.to_bytes()    # Convert number to network byte order
        #print(f"{host}:{port}: snd: {num:>5}")
        nsent = receiver_control.socket.send(net_num)
        log_entry = f"snd 0.00 SYN {num} 0"
        with open(LOG_FILE, "a") as log_file:
            log_file.write(log_entry + "\n")
            print(log_entry)
    if nsent != len(net_num):
        receiver_control.is_alive = False
        sys.exit(f"send: partial/failed send of {nsent} bytes")
    return start_time, 1

def recvACK(s, control, start_time):
    try:
        segment_bytes, receiver_addr = s.recvfrom(SEGMENT_SIZE)
        if len(segment_bytes) < 3:
            print(f"recv: received short message of {segment_bytes} bytes", file=sys.stderr)
        

        segment = STP_segment.from_bytes(segment_bytes)
        curr_time = round(time.time()*1000, 2)
        interval_time = curr_time-start_time
        num = segment.get_seqno()
        

        if random.random() < rlp:
            log_entry = f"drp {interval_time:.2f} ACK {num} 0"
            with open(LOG_FILE, "a") as log_file:
                log_file.write(log_entry + "\n")
                print(log_entry)
            return -1
        else:
            log_entry = f"rcv {interval_time:.2f} ACK {num} 0"
            with open(LOG_FILE, "a") as log_file:
                log_file.write(log_entry + "\n")
                print(log_entry)
            #print(f"{receiver_addr[0]}:{receiver_addr[1]}: rcv: {num:>5} ack123456")
            return num
    except ConnectionRefusedError:
        print(f"recv: connection refused by {control.host}:{control.port}, shutting down...", file=sys.stderr)
        control.is_alive = False
    except socket.timeout:
        print(f"No data within 5 seconds, shutting down.")
    return -1


def send_data(receiver_control, chunk, curr_ACK, start_time, flp, lost_data, rto, total_data, retransmitted_seg, if_block):
    curr_time = round(time.time()*1000, 2)
    interval_time = curr_time - start_time
    
    #print(f"127.0.0.1:{receiver_control.port}: snd: {curr_ACK} ack")
    segment = STP_segment(STP_type=0, STP_seqno=curr_ACK%(2**16), STP_data=chunk.encode())
    net_num = segment.to_bytes()

    if random.random() < flp:
        # Simulate segment loss
        log_entry = f"drp {interval_time:.2f} DATA {curr_ACK%(2**16)} 1000"
        with open(LOG_FILE, "a") as log_file:
            log_file.write(log_entry + "\n")
            print(log_entry)
        
        #if retransmitted_thread is not None and retransmitted_thread.is_alive():
        if if_block == True:
            time.sleep(rto/1000)
            send_data(receiver_control, chunk, curr_ACK, start_time, flp, lost_data, rto, total_data, retransmitted_seg, if_block)
        else:
            retransmitted = threading.Timer(rto / 1000, retransmitted_thread, args=(receiver_control, start_time, flp, lost_data, rto, total_data, retransmitted_seg))
            retransmitted.start()
            if [curr_ACK, chunk] not in lost_data.queue:
                lost_data.put([curr_ACK, chunk])
        retransmitted_seg += 1

    else:
        nsent = receiver_control.socket.send(net_num)
        total_data += len(chunk)
        log_entry = f"snd {interval_time:.2f} DATA {curr_ACK%(2**16)} {len(chunk)}"
        with open(LOG_FILE, "a") as log_file:
            log_file.write(log_entry + "\n")
            print(log_entry)
        received_data.put([curr_ACK, chunk])
        ack_lost_data.put([curr_ACK, chunk])
    return retransmitted_seg

if __name__ == "__main__":
    if len(sys.argv) != NUM_ARGS + 1:
        sys.exit(f"Usage: {sys.argv[0]} sender_port receiver_port txt_file_to_send max_win rto flp rlp")
    host = "127.0.0.1"
    sender_port = parse_port(sys.argv[1])
    receiver_port = parse_port(sys.argv[2])
    txt_file_to_send = parse_file(sys.argv[3])
    max_win = parse_win(sys.argv[4])

    rto = parse_run_time(sys.argv[5])
    flp = parse_loss_probability(sys.argv[6])
    rlp = parse_loss_probability(sys.argv[7])
    if os.path.isfile(LOG_FILE):
        os.remove(LOG_FILE)
    receiver_sock = setup_socket("127.0.0.1", receiver_port)
    sender_sock = setup_socket("127.0.0.1", sender_port)

    # Create a control block for the sender program.
    sender_control = Control("127.0.0.1", sender_port, sender_sock, rto)
    receiver_control = Control("127.0.0.1", receiver_port, receiver_sock, rto)

    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', sender_port))              # bind to `port` on all interfaces

    random.seed()  # Seed the random number generator
    num = random.randrange(2**16)       # Random number in range [0, 65535] 
    start_time, isn_flag = send_isn(receiver_control, host, receiver_port, num)
    #time.sleep(rto/1000)
    curr_ACK = -1
    if isn_flag != -1:
        curr_ACK = recvACK(s, receiver_control, start_time)
    isn_count = 1
    while curr_ACK == -1 or isn_flag == -1:
        curr_time = round(time.time()*1000, 2)
        interval_time = curr_time - start_time
        while interval_time < (rto*isn_count):
            curr_time = round(time.time()*1000, 2)
            interval_time = curr_time - start_time
        isn_count += 1

        if random.random() < flp:
            log_entry = f"drp {interval_time} SYN {num} 0"
            with open(LOG_FILE, "a") as log_file:
                log_file.write(log_entry + "\n")
                print(log_entry)
            isn_flag = -1
        else:
            segment = STP_segment(STP_type=2, STP_seqno=num, STP_data=b'')
            net_num = segment.to_bytes()    # Convert number to network byte order
            #print(f"{host}:{port}: snd: {num:>5}")
            nsent = receiver_control.socket.send(net_num)
            log_entry = f"snd {interval_time} SYN {num} 0"
            with open(LOG_FILE, "a") as log_file:
                log_file.write(log_entry + "\n")
                print(log_entry)
            isn_flag = 1
        if isn_flag == 1:
            curr_ACK = recvACK(s, receiver_control, start_time)     
    #print(curr_ACK)
    # Start the receiver and timer threads.

    
    #receiver.start()
    timer = threading.Timer(rto/1000, timer_thread, args=(sender_control, s))
    receiver = threading.Thread(target=recv_thread, args=(receiver_control, s, start_time, flp, lost_data, rto, 0, 0))
    receiver.start()
    total_data = 0
    retransmitted_seg = 0
    original_seg = 0
    # Send a sequence of random numbers as separate datagrams, until the 
    # timer expires.
    with open(txt_file_to_send, 'r') as file:
        
        while receiver_control.is_alive:

            chunk = file.read(max_win)
            if not chunk:
                
                while not lost_data.empty():
                    tmp_ack, tmp_chunk = lost_data.get()
                    flag = 0
                    count = 0
                    while True:
                        time.sleep(1/1000)
                        count += 1
                        
                        while not received_seqno_queue.empty():
                            seqno = received_seqno_queue.get()
                            if tmp_ack == seqno-1000:
                                flag = 1
                                break
                        if count >= rto:
                            break
                        if flag == 0:
                            previoud_retransmitted_seg = retransmitted_seg
                            retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, True)
                            while previoud_retransmitted_seg != retransmitted_seg:
                                previoud_retransmitted_seg = retransmitted_seg
                                retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, True)
                            
                        break
                    count = 0
                    while True:
                        time.sleep(1/1000)
                        count += 1
                        flag = 0
                        while not received_seqno_queue.empty():
                            seqno = received_seqno_queue.get()
                            if tmp_ack == seqno-1000:
                                flag = 1
                                break
                                break
                        if count >= rto:
                             break

                time.sleep(rto/10000)
                
                # send fin
                segment = STP_segment(STP_type=3, STP_seqno=curr_ACK%(2**16), STP_data=chunk.encode())
                net_num = segment.to_bytes()
                
                curr_time = round(time.time()*1000, 2)
                interval_time = curr_time - start_time
                #print(curr_time, start_time, interval_time, 11111111111111111111111111111111)

                '''
                if -1 == recvACK(s, receiver_control):
                    print("resend fin")
                    segment = STP_segment(STP_type=3, STP_seqno=curr_ACK, STP_data=chunk.encode())
                    net_num = segment.to_bytes()
                    nsent = receiver_control.socket.send(net_num)
                '''
                '''
                fin_recevied_flag = 0
                while not received_seqno_queue.empty():
                    seqno = received_seqno_queue.get()
                    if seqno == curr_ACK+1:
                        fin_recevied_flag = 1
                '''

                if random.random() < flp:
                    # lost fin
                    log_entry = f"drp {interval_time:.2f} FIN {curr_ACK%(2**16)} 0"
                    with open(LOG_FILE, "a") as log_file:
                        log_file.write(log_entry + "\n")
                        print(log_entry)
                    segment = STP_segment(STP_type=3, STP_seqno=curr_ACK%(2**16), STP_data=chunk.encode())
                    net_num = segment.to_bytes()
                    nsent = receiver_control.socket.send(net_num)
                    curr_time = round(time.time()*1000, 2)
                    interval_time = curr_time - start_time
                    log_entry = f"snd {interval_time:.2f} FIN {curr_ACK%(2**16)} 0"
                    with open(LOG_FILE, "a") as log_file:
                        log_file.write(log_entry + "\n")
                        print(log_entry)
                    
                    #recvACK(s, receiver_control, start_time)
                    
                else:
                    nsent = receiver_control.socket.send(net_num)
                    log_entry = f"snd {interval_time:.2f} FIN {curr_ACK%(2**16)} 0"
                    with open(LOG_FILE, "a") as log_file:
                        log_file.write(log_entry + "\n")
                        print(log_entry)

                    #time.sleep(rto/1000)
                    #recvACK(s, receiver_control, start_time)
                    #receiver.join()
                
                    #time.sleep(1)
                '''
                tmp_ack = curr_ACK 
                count = 0
                while True:
                    time.sleep(1/1000)
                    count += 1
                    flag = 0
                    while not received_seqno_queue.empty():
                        seqno = received_seqno_queue.get()
                        if tmp_ack == seqno-1000:
                            flag = 1
                            break
                            break
                    if count >= rto:
                        break
                '''
                receiver_control.is_alive = False
                sender_control.is_alive = False
                timer.start()
                #receiver.join()
                timer.join()
                

                timer.cancel()
                file.close()
                break
            # Log the send and then send the random number.
            # print(f"127.0.0.1:{sender_port}: snd: {num:>5} data")
            

            while lost_data.qsize() >= (max_win // 1000):
                tmp_ack, tmp_chunk = lost_data.get()
                flag = 0
                count = 0
                while True:
                    time.sleep(1/1000)
                    count += 1
                    
                    while not received_seqno_queue.empty():
                        seqno = received_seqno_queue.get()
                        if tmp_ack == seqno-1000:
                            flag = 1
                            break
                            break
                    if count >= rto:
                        break

                if flag == 0:
                    previoud_retransmitted_seg = retransmitted_seg
                    retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)
                    while previoud_retransmitted_seg != retransmitted_seg:
                        previoud_retransmitted_seg = retransmitted_seg
                        retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)
                    
                count = 0

                while True:
                    time.sleep(1/1000)
                    count += 1
                    flag = 0
                    while not received_seqno_queue.empty():
                        seqno = received_seqno_queue.get()
                        if tmp_ack == seqno-1000:
                            flag = 1
                            break
                            break
                    if count >= rto:
                        break

            tmp_data = []
            for i in range(0, len(chunk), SEGMENT_SIZE):
                data = chunk[i:i+SEGMENT_SIZE]
                retransmitted_seg = send_data(receiver_control, data, curr_ACK, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)
                tmp_data.append([curr_ACK, data])
                curr_ACK += len(data)
                total_data += len(data)
                original_seg += 1
                count = 0

            while lost_data.qsize() >= (max_win // 1000):
                tmp_ack, tmp_chunk = lost_data.get()
                flag = 0
                count = 0
                while True:
                    time.sleep(1/1000)
                    count += 1
                    
                    while not received_seqno_queue.empty():
                        seqno = received_seqno_queue.get()
                        if tmp_ack == seqno-1000:
                            flag = 1
                            break
                            break
                    if count >= rto:
                        break

                if flag == 0:
                    previoud_retransmitted_seg = retransmitted_seg
                    retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)
                    while previoud_retransmitted_seg != retransmitted_seg:
                        previoud_retransmitted_seg = retransmitted_seg
                        retransmitted_seg = send_data(receiver_control, tmp_chunk, tmp_ack, start_time, flp, lost_data, rto, total_data, retransmitted_seg, False)
                    
                count = 0

                while True:
                    time.sleep(1/1000)
                    count += 1
                    flag = 0
                    while not received_seqno_queue.empty():
                        seqno = received_seqno_queue.get()
                        if tmp_ack == seqno-1000:
                            flag = 1
                            break
                            break
                    if count >= rto:
                        break

            if not receiver_control.is_alive:
                break
            
            # Sleep for a random amount of time before sending the next message.
            # This is ONLY done for the sake of the demonstration, it should be 
            # removed to maximise the efficiency of the sender.

            #time.sleep(rto/1000)
    
        # Suspend execution here and wait for the threads to finish.
        dict_seqno1 = {}
        while not dict_seqno.empty():
            dict_seqno_num = dict_seqno.get()
            if dict_seqno_num not in dict_seqno1:
                dict_seqno1[dict_seqno_num] = 0
            else:
                dict_seqno1[dict_seqno_num] += 1
        ret_duplicate_ack_rcv = 0
        for i in dict_seqno1:
            ret_duplicate_ack_rcv += dict_seqno1[i]

        log_entry = f"Original data sent: {total_data}\nOriginal data acked: {total_data}\nOriginal segments sent: {original_seg}\nRetransmitted segments: {retransmitted_seg+recv_data_drop}\nDup acks received: {ret_duplicate_ack_rcv}\nData segments dropped: {retransmitted_seg} \nAck segments dropped: {recv_data_drop}"
        with open(LOG_FILE, "a") as log_file:
            log_file.write("\n" + log_entry + "\n")
            print('\n'+log_entry)

    receiver_control.socket.close()  # Close the socket
    sender_control.socket.close()
    print("Shut down complete.")
    file.close()
    sys.exit(0)