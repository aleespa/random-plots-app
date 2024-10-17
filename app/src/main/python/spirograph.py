import numpy as np
from matplotlib import pyplot as plt
import matplotlib.colors
import io
import base64

colors_dark = [
    "#3b3b3b",  # Dark gray
    "#4e5d6c",  # Muted dark blue
    "#4a4d73",  # Muted dark purple
    "#36454f",  # Dark slate gray
    "#555555",  # Medium gray
    "#2c3e50",  # Muted deep teal
    "#34495e",  # Muted navy blue
    "#e74c3c",  # Vibrant red
    "#f39c12",  # Vibrant orange
    "#27ae60",  # Vibrant green
    "#2980b9",  # Vibrant blue
    "#8e44ad",  # Vibrant purple
]
cmap_dark = matplotlib.colors.ListedColormap(colors_dark)
colors_light = [
    "#f39c12",  # Vibrant orange
    "#e74c3c",  # Vibrant red
    "#1abc9c",  # Vibrant teal
    "#3498db",  # Vibrant blue
    "#9b59b6",  # Vibrant purple
    "#2ecc71",  # Vibrant green
    "#e67e22",  # Muted orange
    "#c0392b",  # Muted red
    "#16a085",  # Muted teal
    "#2980b9",  # Muted blue
    "#8e44ad",  # Muted purple
    "#27ae60",  # Muted green
]
cmap_light = matplotlib.colors.ListedColormap(colors_light)
def generate_plot(dark=False):
    t = np.linspace(0, 2 * np.pi, 10000)
    fig, ax = plt.subplots(figsize=(12, 12), dpi=200, tight_layout=True)
    if dark:
        fig.patch.set_facecolor('#302e2b')
    else:
        fig.patch.set_facecolor('#f4f0e7')

    for _ in range(4):
        a, b, c, d = sorted(np.random.randint(1, 26, 4))
        k, l = a / b, c / d
        if dark:
            color = cmap_dark(np.random.uniform())
        else:
            color = cmap_light(np.random.uniform())
        plot_spiro(t, k, l, ax, color)
        # Save the figure to a buffer without extra white space

    ax.axis('off')
    buffer = io.BytesIO()
    plt.savefig(buffer, format='jpg', bbox_inches='tight', pad_inches=0)
    buffer.seek(0)

    return buffer


def spiro(t: np.array,
          k: float = 0.5,
          l: float = 0.5):
    return ((1 - k) * np.exp(1j * t)
            + k * l * np.exp(- 1j * t * (1 - k) / k))

def plot_spiro(t, k, l, ax, color):
    s = spiro(100 * t, k, l)
    ax.plot(s.real, s.imag, lw=3, alpha=0.9,
            color=color)

def create_image(dark_mode=False):
    buffer = generate_plot(dark_mode)
    image_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
    return image_data