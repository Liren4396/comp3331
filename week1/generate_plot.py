#!/usr/bin/python3
import matplotlib.pyplot as plt

# Data
locations = ['Darwin', 'Sao Paulo', 'Edinburgh']
"""
Follow the provided instructions to compute distances and ratios.
Uncomment the designated lists and replace them with the actual values.
Once updated, execute the command 'python3 generate_plot.py' to generate the plot.
Feel free to make any necessary customizations to suit your preferences.
"""
distances = [375.64, 13490.89, 16880.66]
ratios = [43.395, 7.602, 4.771]

# Create scatter plot
plt.scatter(distances, ratios, color='blue')

# Label each point with location name with adjusted text placement
for i, location in enumerate(locations):
    plt.annotate(location, (distances[i], ratios[i]), textcoords="offset points", xytext=(0,8), ha='center', va='bottom')

# Set labels and title
plt.xlabel('Distance (km)')
plt.ylabel('Ratio of Min.RTT to Propagation Delay')
plt.title('Distance vs Ratio for Different Locations')

# Set minimum and maximum values for x-axis and y-axis
plt.xlim(0, max(distances) + 3000)  # Adjust the upper limit as needed
plt.ylim(0, max(ratios) + 1)  # Adjust the upper limit as needed

# Add gridlines
plt.grid(True, linestyle='--', alpha=0.7)

# Show the plot
plt.show()