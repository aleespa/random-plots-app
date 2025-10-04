import io

import numpy as np
from matplotlib import pyplot as plt


def create_image(seed=0, bg_color=(0, 0, 0), cmap=None):
    buffer = generate_plot(seed, bg_color, cmap)
    plt.close()
    return buffer.getvalue()


def generate_plot(seed, bg_color="#000000", cmap=None):
    rng = np.random.default_rng(seed)
    fig, ax = plt.subplots(figsize=(12, 12), dpi=200, tight_layout=True)
    fig.patch.set_facecolor(bg_color)
    variance = rng.exponential(1)
    n = 150
    for z in np.linspace(1, 20, 100):
        theta = np.linspace(0, 2 * np.pi, n)
        c, s = np.cos(theta), np.sin(theta)
        r0 = rng.uniform(1 * z, (1 + variance) * z)
        r = np.array([r0] + list(rng.uniform(1 * z, (1 + variance) * z, n - 2)) + [r0])

        ax.plot(c * r, r * s, lw=1.1, color=cmap(z / 20))

    ax.axis('off')

    buffer = io.BytesIO()
    plt.savefig(buffer, format='jpg', bbox_inches='tight', pad_inches=0)
    buffer.seek(0)

    return buffer
