import io
import base64

import numpy as np
from matplotlib import pyplot as plt


def vectorized_sample_complex_pairs(sample_size: int):
    # Sample 2n random angles from 0 to 2*pi (2 for each pair)
    thetas = np.random.uniform(0, 2 * np.pi, size=3 * sample_size)

    # Compute complex numbers
    zs = np.random.uniform(0.5, 1.2) * np.exp(1j * thetas)

    # Reshape to get n pairs
    pairs = zs.reshape(sample_size, 3)

    return pairs


def calculate_matrix(t: np.array, r1, r2):
    return np.array([[t[2] * 1j, -t[2], -t[2], r1, 1j],
                     [-1, 1, 0, 1, 1],
                     [t[1], r2, -1j, t[2], 1j],
                     [1j, t[0], 1j, 1j, 1j],
                     [1j, 2, -1, -1, 1j]]).T


def calculate_eigenvalues(x: np.array):
    return np.linalg.eigvals(x)


def generate_plot(x, y, dark_mode=False, bg_color=(0, 0, 0)):
    # Create a figure with adjusted layout
    fig, ax = plt.subplots(figsize=(12, 12), dpi=100, tight_layout=True)
    if dark_mode:
        fig.patch.set_facecolor(bg_color)
    else:
        fig.patch.set_facecolor(bg_color)
    # Create scatter plot without axes
    ax.scatter(x, y, s=1.5,
               color='w' if dark_mode else "k",
               lw=0, alpha=0.9)
    ax.set_xlim(-3, 3.5)
    ax.set_ylim(-3, 3.5)

    # Remove axes lines and labels
    ax.axis('off')

    # Save the figure to a buffer without extra white space
    buffer = io.BytesIO()
    plt.savefig(buffer, format='jpg', bbox_inches='tight', pad_inches=0)
    buffer.seek(0)

    return buffer


def create_image(dark_mode=False, bg_color=(0, 0, 0)):
    sample_size = 10000
    sample = vectorized_sample_complex_pairs(sample_size)
    r1 = np.random.uniform(-1, 1) + np.random.uniform(-1, 1) * 1j
    r2 = np.random.uniform(-1, 1) + np.random.uniform(-1, 1) * 1j
    Z = np.array([calculate_eigenvalues(calculate_matrix(t, r1, r2)) for t in sample]).ravel()
    x = Z.real
    y = Z.imag
    buffer = generate_plot(x, y, dark_mode, bg_color)
    image_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
    plt.close()
    return image_data
