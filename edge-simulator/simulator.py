import json
import os
import random
import time
from datetime import datetime, timezone

import paho.mqtt.client as mqtt


BROKER_HOST = os.getenv("MQTT_HOST", "localhost")
BROKER_PORT = int(os.getenv("MQTT_PORT", "1883"))
DEVICE_COUNT = int(os.getenv("DEVICE_COUNT", "3"))
INTERVAL_SECONDS = int(os.getenv("INTERVAL_SECONDS", "5"))


def build_payload(device_id: str, tick: int) -> dict:
    base_temp = 55 + random.random() * 18
    base_voltage = 210 + random.random() * 24

    if tick % 9 == 0 and device_id == "dev001":
        base_temp = 84 + random.random() * 8
    if tick % 13 == 0 and device_id == "dev002":
        base_voltage = 165 + random.random() * 10

    return {
        "deviceId": device_id,
        "temperature": round(base_temp, 2),
        "voltage": round(base_voltage, 2),
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


def main() -> None:
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="edge-simulator")
    client.connect(BROKER_HOST, BROKER_PORT, keepalive=60)
    client.loop_start()

    devices = [f"dev{i:03d}" for i in range(1, DEVICE_COUNT + 1)]
    tick = 0
    print(f"Edge simulator started, broker={BROKER_HOST}:{BROKER_PORT}, devices={devices}")
    try:
        while True:
            tick += 1
            for device_id in devices:
                payload = build_payload(device_id, tick)
                topic = f"iot/device/{device_id}/telemetry"
                client.publish(topic, json.dumps(payload), qos=1)
                print(topic, payload)
            time.sleep(INTERVAL_SECONDS)
    except KeyboardInterrupt:
        print("Edge simulator stopped.")
    finally:
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
