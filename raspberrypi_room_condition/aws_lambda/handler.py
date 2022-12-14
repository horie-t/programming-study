import boto3
import time

DatabaseName = 'RoomCondition'
TableName = 'conditions'

def current_milli_time():
    return round(time.time() * 1000)

client = boto3.client('timestream-write', region_name='us-west-2')

def write_record(event, context):
    print(event)

    temperature = event['temperature']
    co2 = event['co2']
    humidity = event['humidity']

    current_time = str(current_milli_time())

    dimensions = [
        {'Name': 'deviceId', 'Value': '1'},
    ]

    co2_record = {
        'Dimensions': dimensions,
        'MeasureName': 'co2',
        'MeasureValue': str(co2),
        'MeasureValueType': 'DOUBLE',
        'Time': current_time
    }

    temperature_record = {
        'Dimensions': dimensions,
        'MeasureName': 'temperature',
        'MeasureValue': str(temperature),
        'MeasureValueType': 'DOUBLE',
        'Time': current_time
    }

    humidity_record = {
        'Dimensions': dimensions,
        'MeasureName': 'humidity',
        'MeasureValue': str(humidity),
        'MeasureValueType': 'DOUBLE',
        'Time': current_time
    }

    records = [co2_record, temperature_record, humidity_record]

    result = client.write_records(DatabaseName=DatabaseName, TableName=TableName, Records=records, CommonAttributes={})

    return result
