#!/usr/bin/env python3

import time
import json
import sys

import digitalio
import board
from PIL import Image, ImageDraw, ImageFont
from adafruit_rgb_display import ili9341

from scd30_i2c import SCD30

from awscrt import io, mqtt, auth, http
from awsiot import mqtt_connection_builder

# Define ENDPOINT, CLIENT_ID, PATH_TO_CERTIFICATE, PATH_TO_PRIVATE_KEY, PATH_TO_AMAZON_ROOT_CA_1, MESSAGE, TOPIC, and RANGE
ENDPOINT = "XXXXXXXXXXXXXX-XXX.iot.us-west-2.amazonaws.com"
CLIENT_ID = "raspberrypi-room-condition"
PATH_TO_CERTIFICATE = "certificates/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-certificate.pem.crt"
PATH_TO_PRIVATE_KEY = "certificates/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-private.pem.key"
PATH_TO_AMAZON_ROOT_CA_1 = "certificates/AmazonRootCA1.pem"
TOPIC = "room_condition"

def init_display():
    """Initialize display, and return display object"""
    # Configuration for CS and DC pins (these are PiTFT defaults):
    cs_pin = digitalio.DigitalInOut(board.CE0)
    dc_pin = digitalio.DigitalInOut(board.D25)
    reset_pin = digitalio.DigitalInOut(board.D24)

    # Config for display baudrate (default max is 24mhz):
    BAUDRATE = 24000000

    # Setup SPI bus using hardware SPI:
    spi = board.SPI()

    # pylint: disable=line-too-long
    # Create the display:
    disp = ili9341.ILI9341(
        spi,
        rotation=0,  # 2.2", 2.4", 2.8", 3.2" ILI9341
        cs=cs_pin,
        dc=dc_pin,
        rst=reset_pin,
        baudrate=BAUDRATE,
    )

    return disp

disp = init_display()

height = disp.height
width = disp.width

image = Image.new("RGB", (width, height))

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)

# Draw a black filled box to clear the image.
draw.rectangle((0, 0, width, height), outline=0, fill=(0, 0, 0))
disp.image(image)


# Load a ipaexfont-gothic font.
font1 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 70)
font2 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 55)
font3 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 25)
font4 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 22)
font5 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 17)
font6 = ImageFont.truetype("/usr/share/fonts/opentype/ipaexfont-gothic/ipaexg.ttf", 12)

text_color1 = "#D9E5FF"

# Spin up resources
event_loop_group = io.EventLoopGroup(1)
host_resolver = io.DefaultHostResolver(event_loop_group)
client_bootstrap = io.ClientBootstrap(event_loop_group, host_resolver)
mqtt_connection = mqtt_connection_builder.mtls_from_path(
            endpoint=ENDPOINT,
            cert_filepath=PATH_TO_CERTIFICATE,
            pri_key_filepath=PATH_TO_PRIVATE_KEY,
            client_bootstrap=client_bootstrap,
            ca_filepath=PATH_TO_AMAZON_ROOT_CA_1,
            client_id=CLIENT_ID,
            clean_session=False,
            keep_alive_secs=6
            )

print("Connecting to {} with client ID '{}'...".format(ENDPOINT, CLIENT_ID), file=sys.stderr)

# Make the connect() call
connect_future = mqtt_connection.connect()
# Future.result() waits until a result is available
connect_future.result()
print("Connected!", file=sys.stderr)

# Setup SCD30
scd30 = SCD30()

measurment_interval_sec = 10
scd30.set_measurement_interval(measurment_interval_sec)
scd30.set_auto_self_calibration(active=True)
scd30.start_periodic_measurement()

time.sleep(2)

day_of_week = ["月", "火", "水", "木", "金", "土", "日"]

while True:
    if not scd30.get_data_ready():
        time.sleep(0.2)
        continue

    m = scd30.read_measurement()
    if m is None:
        time.sleep(measurment_interval_sec)
        continue

    local_time = time.localtime()
    current_date = '{}月{}日({})'.format(local_time.tm_mon, local_time.tm_mday, day_of_week[local_time.tm_wday])
    current_time = time.strftime('%H:%M', local_time)
    print(time.strftime('%Y-%m-%dT%H:%M:%S%z', local_time) + "," + f"{m[0]:.1f},{m[1]:.1f},{m[2]:.1f}")
    
    # Draw a black filled box to clear the image.
    draw.rectangle((0, 0, width, height), outline=0, fill=(0, 0, 0))

    # date and time
    draw.text((width / 2, 15), current_date, align='center', anchor="ma", font=font3, fill=text_color1)
    draw.text((width / 2, 55), current_time, align='center', anchor="ma", font=font1, fill=text_color1)
    
    # Temperature
    temp =format(float(m[1]), '.1f')
    draw.text((195, 145), temp, align='right', anchor='rt', font=font2, fill=text_color1)
    draw.text((197, 145), "℃", font=font3, fill=text_color1)

    # Concentration of CO2
    co2 =format(float(m[0]), '.1f')
    draw.text((195, 205), co2, align='right', anchor='rt', font=font2, fill=text_color1)
    draw.text((197, 205), "ppm", font=font5, fill=text_color1)
    draw.text((197, 230), "CO", font=font5, fill=text_color1)
    draw.text((225, 235), "2", font=font6, fill=text_color1)

    # Humidity
    humi =format(float(m[2]), '.1f')
    draw.text((195, 265), humi, align='right', anchor='rt', font=font2, fill=text_color1)
    draw.text((197, 265), "%", font=font4, fill=text_color1)
    draw.text((197, 288), "RH", font=font4, fill=text_color1)

    disp.image(image)

    message = {"temperature" : temp, "co2": co2, "humidity": humi}
    mqtt_connection.publish(topic=TOPIC, payload=json.dumps(message), qos=mqtt.QoS.AT_LEAST_ONCE)
    print("Published: '" + json.dumps(message) + "' to the topic: '" + TOPIC + "'", file=sys.stderr)

    time.sleep(measurment_interval_sec)
