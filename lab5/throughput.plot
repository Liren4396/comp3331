# throughput.plot

# Set the output file name
set output "TCPThroughput.png"

# Set the title of the plot
set title "TCP Throughput"

# Set the labels for x and y axes
set xlabel "Time (s)"
set ylabel "Throughput (Mbps)"

# Plot the throughput for tcp1
plot "tcp1.tr" using 1:2 with lines lw 2 title "Flow tcp1", \
     "tcp2.tr" using 1:2 with lines lw 2 title "Flow tcp2"
pause -1
