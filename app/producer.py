import pika
import json
from .rabbitmq_config import setup_rabbitmq, WALLET_EXCHANGE, AUTH_EXCHANGE, ROUTING_KEY_EMAIL_VERIFICATION, ROUTING_KEY_EMAIL_OTP, ROUTING_KEY_EMAIL_RESET_PASSWORD

class Producer:
    def __init__(self):
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host="localhost")
        )
        self.channel = self.connection.channel()
        setup_rabbitmq()

    def send_message(self, exchange, routing_key, message):
        self.channel.basic_publish(
            exchange=exchange,
            routing_key=routing_key,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2,  # make message persistent
            ))
        print(f" [x] Sent {message} to {exchange} with routing key {routing_key}")

    def send_verification_email(self, email, username, verification_link):
        message = {
            "email": email,
            "username": username,
            "verification_link": verification_link
        }
        self.send_message(AUTH_EXCHANGE, ROUTING_KEY_EMAIL_VERIFICATION, message)

    def send_otp_email(self, email, username, otp):
        message = {
            "email": email,
            "username": username,
            "otp": otp
        }
        self.send_message(AUTH_EXCHANGE, ROUTING_KEY_EMAIL_OTP, message)

    def send_reset_password_email(self, email, username, reset_link):
        message = {
            "email": email,
            "username": username,
            "reset_link": reset_link
        }
        self.send_message(AUTH_EXCHANGE, ROUTING_KEY_EMAIL_RESET_PASSWORD, message)

    def close(self):
        self.connection.close()