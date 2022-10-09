```python
!pwd
```

    /home/tetsuya/repo/experiment/sagemaker_whisper



```python
%cd /home/tetsuya/repo/experiment/sagemaker_whisper
```

    /home/tetsuya/repo/experiment/sagemaker_whisper



```python
!./build_and_push.sh sagemaker-whisper
```

    WARNING! Your password will be stored unencrypted in /home/tetsuya/.docker/config.json.
    Configure a credential helper to remove this warning. See
    https://docs.docker.com/engine/reference/commandline/login/#credentials-store
    
    Login Succeeded
    Sending build context to Docker daemon   59.9kB
    Step 1/9 : FROM nvidia/cuda:11.7.1-runtime-ubuntu20.04
     ---> 9a178fee7c22
    Step 2/9 : RUN apt-get -y update
     ---> Using cache
     ---> 4d7452ab4cd0
    Step 3/9 : RUN DEBIAN_FRONTEND=noninteractive apt-get -y install         python3-pip         python3-setuptools         ffmpeg         git          nginx          ca-certificates     && rm -rf /var/lib/apt/lists/*
     ---> Using cache
     ---> 40b104673e9f
    Step 4/9 : RUN pip --no-cache-dir install git+https://github.com/openai/whisper.git setuptools-rust flask gunicorn
     ---> Using cache
     ---> c78db5f82d57
    Step 5/9 : ENV PYTHONUNBUFFERED=TRUE
     ---> Using cache
     ---> b587c468fcbf
    Step 6/9 : ENV PYTHONDONTWRITEBYTECODE=TRUE
     ---> Using cache
     ---> b0aec232dd00
    Step 7/9 : ENV PATH="/opt/program:${PATH}"
     ---> Using cache
     ---> 31ed3704a77c
    Step 8/9 : COPY src /opt/program
     ---> baebe7150fd8
    Step 9/9 : WORKDIR /opt/program
     ---> Running in 85e06a8a103f
    Removing intermediate container 85e06a8a103f
     ---> bebcb5d3d136
    Successfully built bebcb5d3d136
    Successfully tagged sagemaker-whisper:latest
    The push refers to repository [269376826173.dkr.ecr.us-west-2.amazonaws.com/sagemaker-whisper]
    
    [1Bd455dacd: Preparing 
    [1B187dcf7c: Preparing 
    [1B03cde4dc: Preparing 
    [1B5e983054: Preparing 
    [1Be2e9a5a0: Preparing 
    [1Bec63d09d: Preparing 
    [1B360caa90: Preparing 
    [1Bb23786a0: Preparing 
    [1Bd37653c4: Preparing 
    [1B56016ec1: Preparing 
    [11B455dacd: Pushed lready exists 7kB[6A[2K[3A[2K[1A[2K[11A[2Klatest: digest: sha256:70970b6fbf7f39f061eb8287e47d860520e69930018b17e5265d5e2439d486ed size: 2637



```python
%cd model
```

    /home/tetsuya/repo/experiment/sagemaker_whisper/model



```python
!curl -O "https://openaipublic.azureedge.net/main/whisper/models/345ae4da62f9b3d59415adc60127b97c714f32e89e936602e85993674d08dcb1/medium.pt"
```

      % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                     Dload  Upload   Total   Spent    Left  Speed
    100 1457M  100 1457M    0     0  9570k      0  0:02:35  0:02:35 --:--:-- 10.2M01:46 10.8M 777M    0     0  10.0M      0  0:02:25  0:01:17  0:01:08 10.9M 7109k



```python
!tar -czf model.tar.gz large.pt
```


```python
!tar -czf model.tar.gz medium.pt
```


```python
import boto3

role_name = "SageMaker-local"

iam = boto3.client("iam")
role = iam.get_role(RoleName=role_name)["Role"]["Arn"]
```


```python
import sagemaker as sage

sess = sage.Session()
```


```python
model_location = sess.upload_data("./model.tar.gz", key_prefix="whisper/model")
```


```python
account = sess.boto_session.client('sts').get_caller_identity()['Account']
region = sess.boto_session.region_name
image = '{}.dkr.ecr.{}.amazonaws.com/sagemaker-whisper:latest'.format(account, region)
```


```python
model_name = 'whisper'

container_params = {
    "Image": image,
    "ModelDataUrl": model_location,
}

model = sess.create_model(model_name, role, container_params)
```


```python
sagemaker_client = sess.boto_session.client('sagemaker', region_name='us-west-2')
```


```python
model = "whisper"
endpoint_config_name = "whisper-config"

create_endpoint_config_response = sagemaker_client.create_endpoint_config(
    EndpointConfigName=endpoint_config_name,
    ProductionVariants=[
        {
            "VariantName": "variant1",
            "ModelName": model, 
            "InstanceType": "ml.p3.2xlarge",
            "InitialInstanceCount": 1
        }
    ],
    AsyncInferenceConfig={
        "OutputConfig": {
            "S3OutputPath": f"s3://{sess.default_bucket()}/whisper/output"
        },
    }
)

print(f"Created EndpointConfig: {create_endpoint_config_response['EndpointConfigArn']}")
```

    Created EndpointConfig: arn:aws:sagemaker:us-west-2:269376826173:endpoint-config/whisper-config



```python
endpoint_name = 'whisper'
endpoint_config_name = "whisper-config"

create_endpoint_response = sagemaker_client.create_endpoint(
                                            EndpointName=endpoint_name, 
                                            EndpointConfigName=endpoint_config_name)
```


```python
autoscaling_client = sess.boto_session.client('application-autoscaling') 

variant_name = "variant1"
endpoint_name = 'whisper'

resource_id=f'endpoint/{endpoint_name}/variant/{variant_name}' 

response = autoscaling_client.register_scalable_target(
    ServiceNamespace='sagemaker', 
    ResourceId=resource_id,
    ScalableDimension='sagemaker:variant:DesiredInstanceCount',
    MinCapacity=0,  
    MaxCapacity=1
)

response = autoscaling_client.put_scaling_policy(
    PolicyName='Invocations-ScalingPolicy',
    ServiceNamespace='sagemaker', 
    ResourceId=resource_id, 
    ScalableDimension='sagemaker:variant:DesiredInstanceCount',
    PolicyType='TargetTrackingScaling',
    TargetTrackingScalingPolicyConfiguration={
        'TargetValue': 1.0, 
        'CustomizedMetricSpecification': {
            'MetricName': 'ApproximateBacklogSizePerInstance',
            'Namespace': 'AWS/SageMaker',
            'Dimensions': [
                {'Name': 'EndpointName', 'Value': endpoint_name }
            ],
            'Statistic': 'Average',
        },
        'ScaleInCooldown': 120,
        'ScaleOutCooldown': 120
    }
)
```


```python
sagemaker_runtime = boto3.client("sagemaker-runtime", region_name='us-west-2')

input_location = f"s3://{sess.default_bucket()}/whisper/input/001-sibutomo.mp3"

response = sagemaker_runtime.invoke_endpoint_async(
                            EndpointName=endpoint_name, 
                            InputLocation=input_location)
```
