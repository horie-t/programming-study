from vpython import *

scene = canvas()
graph(title='VPython 2D Curve', xtitle='x', ytitle='f(x)',
      width=600, height=450)
plot1 = gcurve(color=color.red)
for x in arange(0, 8.1, 0.1):
    plot1.plot(pos=(x , 5.0 * cos(2*x) * exp(-0.4*x)))

graph(title='VPython 2D Dots', xtitle='x', ytitle='f(x)',
      width=600, height=450, forground=color.white, background=color.black)
plot2 = gdots(color=color.white)
for x in arange(-5, 5.1, 0.1):
    plot2.plot(pos=(x, cos(x)))