import base64
import io

import numpy as np
from matplotlib import pyplot as plt
import matplotlib as mpl


def generate_plot(seed, bg_color=(0, 0, 0), dark_mode=True):
    rng = np.random.default_rng(seed)

    dark_background_colormaps = [
        'Spectral', 'viridis', 'plasma', 'inferno', 'cividis',
        'YlOrRd', 'RdPu', 'spring', 'summer', 'autumn', 'winter',
        'cool', 'Wistia', 'hot', 'afmhot', 'copper', 'gist_heat',
        'binary', 'gist_yarg', 'gist_gray', 'gray', 'bone', 'pink'
    ]

    light_background_colormaps = [
        'gist_heat', 'binary', 'gist_yarg', 'gist_gray', 'gray',
        'bone', 'pink'
    ]
    if dark_mode:
        colormaps = dark_background_colormaps
    else:
        colormaps = light_background_colormaps

    colormap = rng.choice(colormaps)
    B = rng.uniform(-1, -0.999)
    C = rng.uniform(-0.5, 0.5)

    fig, ax = plt.subplots(figsize=(12, 12), dpi=200, tight_layout=True)
    fig.patch.set_facecolor(bg_color)

    n_loops = 256
    n_points = 500
    r = rng.normal(0, 1, (2, n_loops))
    x = np.zeros((n_points, n_loops))
    y = np.zeros((n_points, n_loops))
    x[0, :] = 1 + r[1, :] - C * abs(r[0, :])
    y[0, :] = B * r[0, :]
    for j in range(1, n_points):
        x[j, :] = 1 + y[j - 1, :] - C * abs(x[j - 1, :])
        y[j, :] = B * x[j - 1, :]

    x_flat = x.flatten()
    y_flat = y.flatten()
    colors = np.repeat(mpl.colormaps[colormap](np.linspace(0, 1, n_loops)), n_points, axis=0)

    # Single scatter call
    ax.scatter(
        x_flat,
        y_flat,
        s=1,
        color=colors,
        alpha=0.9,
        lw=0,
    )

    ax.set_xlim(-1.4, 2.5)
    ax.set_ylim(-2.5, 1.4)
    ax.axis('off')
    buffer = io.BytesIO()
    plt.savefig(buffer, format='jpg', bbox_inches='tight', pad_inches=0)
    buffer.seek(0)

    return buffer


def create_image(seed=0, dark_mode=True, bg_color=(0, 0, 0)):
    buffer = generate_plot(seed, bg_color, dark_mode)
    image_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
    plt.close()
    return image_data
