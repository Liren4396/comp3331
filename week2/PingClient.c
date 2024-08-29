#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <fcntl.h>
#include <time.h>
#include <limits.h>

#define MIN(i, j) (((i) < (j)) ? (i) : (j))
#define MAX(i, j) (((i) > (j)) ? (i) : (j))

#define PING_MSG "PING"
#define MAX_TRIES 20
#define TIMEOUT_MS 600
#define SERVER_IP "127.0.0.1"

void die(const char *s) {
    perror(s);
    exit(1);
}
long find_min(long rtt[21]) {
    long min = LONG_MAX;
    for (int i = 0; i < 20; i++) {
        min = MIN(min, rtt[i]);
    }
    return min;
}
long find_max(long rtt[21]) {
    long max = LONG_MIN;
    for (int i = 0; i < 20; i++) {
        max = MAX(max, rtt[i]);
    }
    return max;
}
long find_ave(long rtt[21]) {
    long average = 0;
    for (int i = 0; i < 20; i++) {
        average += rtt[i];
    }
    average /= 20;
    return average;
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <host> <port>\n", argv[0]);
        exit(1);
    }
    srand(time(NULL));
    int random = rand() % 10000 + 50000;
    char *host = argv[1];
    int port = atoi(argv[2]);
    struct sockaddr_in server_addr;

    // Create socket
    int sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (sockfd < 0) {
        die("socket");
    }

    // Set socket to non-blocking mode
    if (fcntl(sockfd, F_SETFL, O_NONBLOCK) < 0) {
        die("fcntl");
    }

    // Set server address
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    if (inet_aton(host, &server_addr.sin_addr) == 0) {
        fprintf(stderr, "Invalid address: %s\n", host);
        exit(1);
    }
    int start_num = random;
    // Send ping requests
    struct timeval start_time, end_time;
    long rtts[21];
    int count = 0;
    for (int seq = start_num; seq < start_num+MAX_TRIES; seq++) {
        
        char msg[50];
        time_t now = time(NULL);
        sprintf(msg, "%s %d %ld\r\n", PING_MSG, seq, now);

        
        gettimeofday(&start_time, NULL);

        // Send ping request
        ssize_t bytes_sent = sendto(sockfd, msg, strlen(msg), 0,
                                    (struct sockaddr *)&server_addr, sizeof(server_addr));
        if (bytes_sent < 0) {
            die("sendto");
        }
        
        // Receive ping response with timeout
        char buffer[1024];
        memset(buffer, 0, 128);
        
        
        int len = sizeof(server_addr);
        ssize_t bytes_received = recvfrom(sockfd, buffer, sizeof(buffer), 0, (struct sockaddr *)&server_addr, &len);
        struct timeval timeout = {0, TIMEOUT_MS * 1000};
        fd_set readfds;
        FD_ZERO(&readfds);
        FD_SET(sockfd, &readfds);
        
        int ready = select(sockfd + 1, &readfds, NULL, NULL, &timeout);    

        if (ready < 0) {
            die("select");
        } else if (ready == 0) {
            printf("Ping to %s:%d, seq = %d, timeout\n", host, port, seq);
            rtts[count++] = 120;
        } else {
            // Record end time
            struct timeval end_time;
            gettimeofday(&end_time, NULL);

            // Calculate RTT
            long rtt_ms = (end_time.tv_sec - start_time.tv_sec ) * 1000 +
                        (end_time.tv_usec - start_time.tv_usec) / 1000;
            rtts[count++] = rtt_ms;
            printf("Ping to %s:%d, seq = %d, rtt = %ld ms\n", host, port, seq, rtt_ms);
        }
    }
    long rtt_min = find_min(rtts);
    long rtt_max = find_max(rtts);

    printf("minimum = %ld ms , maximum = %ld ms, average = %ld ms\n", rtt_min, rtt_max, find_ave(rtts));
    close(sockfd);
    return 0;
}
