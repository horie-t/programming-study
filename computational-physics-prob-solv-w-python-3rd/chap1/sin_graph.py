from vpython import *

scene = canvas()
graph(title = 'sin関数', xtitle = 'x', ytitle = 'sin(x)')
sin_plot = gcurve(color = color.red)
for x in arange(0.0, 2.0 * pi + 0.1, 0.1):
  sin_plot.plot(x, sin(x))
