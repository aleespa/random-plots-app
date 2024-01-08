import io
import base64
import matplotlib.pylab as plt
import numpy as np
from math import cos, sin,log,tan,pi


def generate_plot():

    fig, ax = plt.subplots(figsize=(12, 12), dpi=100, tight_layout=True)
    fig.patch.set_facecolor('k')
    sides = np.random.choice([4, 5, 6, 7])
    for z in np.linspace(0,5,50):
        plt.plot([cos(t+z)*z for t in np.linspace(0,2*pi,int(sides))],
                 [sin(t+z)*z for t in np.linspace(0,2*pi,int(sides))],
                 lw=3,
                 color=plt.cm.Spectral(z/5))
    ax.axis('off')
    buffer = io.BytesIO()
    plt.savefig(buffer, format='jpg', bbox_inches='tight', pad_inches=0)
    buffer.seek(0)

    return buffer
def create_image():
    buffer = generate_plot()
    image_data = base64.b64encode(buffer.getvalue()).decode('utf-8')
    return image_data