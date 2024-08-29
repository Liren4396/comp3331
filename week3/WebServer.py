import socket
import os
import sys

def handle_client_connection(client_socket):
    request_data = client_socket.recv(1024).decode('utf-8')
    headers = request_data.split('\n')
    file_requested = headers[0].split()[1]
    
    if file_requested == '/':
        file_requested = '/index.html'

    if os.path.isfile('.' + file_requested):
        with open('.' + file_requested, 'rb') as f:
            response_data = f.read()
        response_headers = 'HTTP/1.1 200 OK\n\n'
    else:
        response_data = b'<h1>404 Not Found</h1>'
        response_headers = 'HTTP/1.1 404 Not Found\nContent-Type: text/html\n\n'

    response = response_headers.encode('utf-8') + response_data
    client_socket.sendall(response)
    client_socket.close()

def run_web_server(port):
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('127.0.0.1', port))
    server_socket.listen(5)
    print(f"Server is listening on port {port}...")
    
    while True:
        client_socket, addr = server_socket.accept()
        print(f"Accepted connection from {addr[0]}:{addr[1]}")
        handle_client_connection(client_socket)

if __name__ == "__main__":
    print(sys.argv)
    if len(sys.argv) < 2:
        print("PLS input python3 WebServer port")
        exit()
    port = int(sys.argv[1])
    run_web_server(port)
