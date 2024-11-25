import numpy as np
from matplotlib import pyplot as plt
import matplotlib.colors
import io
import base64

colors_dark = [
    "#4c4c4c",  # Brightened dark gray
    "#5e7a8c",  # Vibrant dark blue
    "#6367a8",  # Vibrant dark purple
    "#4b6673",  # Vibrant slate gray
    "#666666",  # Brighter medium gray
    "#3a5f75",  # Vibrant deep teal
    "#466d8f",  # Vibrant navy blue
    "#ff5f4d",  # Brighter vibrant red
    "#ffa622",  # Brighter vibrant orange
    "#2dcc71",  # Brighter vibrant green
    "#3498db",  # Brighter vibrant blue
    "#a56de2",  # Brighter vibrant purple
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
def generate_plot(dark=False,bg_color=(0,0,0)):
    t = np.linspace(0, 2 * np.pi, 10000)
    fig, ax = plt.subplots(figsize=(12, 12), dpi=200, tight_layout=True)
    if dark:
        fig.patch.set_facecolor(bg_color)
    else:
        fig.patch.set_facecolor(bg_color)

    for _ in range(4):
        k, l = np.random.uniform(0,1,2)
        if dark:
            color = cmap_dark(np.random.uniform())
        else:
            color = cmap_light(np.random.uniform())
        plot_spiro(t, k, l, ax, color)

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
    ax.plot(s.real, s.imag, lw=1, alpha=0.9,
            color=color)

def create_image(dark_mode=False, color=(0,0,0)):
    buffer = generate_plot(dark_mode, color)
    image_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
    return image_data