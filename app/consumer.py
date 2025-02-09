import pika
import json
from .services.notification_service import NotificationService


class Consumer:
    def __init__(self):
        print(" [*] Initializing RabbitMQ Consumer...")  # Debug log
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host="localhost")
        )
        self.channel = self.connection.channel()
        print(" [✓] Connected to RabbitMQ")  # Debug log

    def start_consuming(self):
        print(" [*] Consumer started. Waiting for messages...")  # Debug log
        self.channel.basic_consume(queue="email_verification_queue",
                                   on_message_callback=self.process_verification_email, auto_ack=True)
        self.channel.basic_consume(
            queue="email_otp_queue", on_message_callback=self.process_otp_email, auto_ack=True)
        self.channel.basic_consume(queue="email_reset_password_queue",
                                   on_message_callback=self.process_reset_password_email, auto_ack=True)
        self.channel.start_consuming()

    def process_verification_email(self, ch, method, properties, body):
        print(f" [x] Received message: {body}")  # Debugging print
        message = json.loads(body)
        NotificationService.send_verification_email(
            message['email'], message['username'], message['verification_link'])
        print(f" [x] Sent verification email to {message['email']}")

    def process_otp_email(self, ch, method, properties, body):
        print(f" [x] Received message: {body}")  # Debugging print
        message = json.loads(body)
        NotificationService.send_otp_email(
            message['email'], message['username'], message['otp'])
        print(f" [x] Sent OTP email to {message['email']}")

    def process_reset_password_email(self, ch, method, properties, body):
        print(f" [x] Received message: {body}")  # Debugging print
        message = json.loads(body)
        NotificationService.send_reset_password_email(
            message['email'], message['username'], message['reset_link'])
        print(f" [x] Sent reset password email to {message['email']}")

    def close(self):
        print(" [!] Closing RabbitMQ connection...")  # Debug log
        self.connection.close()
