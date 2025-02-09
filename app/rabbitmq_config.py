import os
import pika

# Wallet Exchange & Queues
WALLET_EXCHANGE = "wallet.notifications"
CREDIT_WALLET_QUEUE = "credit.wallet"
DEBIT_WALLET_QUEUE = "debit.wallet"
DEPOSIT_WALLET_QUEUE = "deposit.wallet"
MAINTENANCE_DEDUCTION_QUEUE = "maintenance.service"

ROUTING_KEY_CREDIT_WALLET = "wallet.credit"
ROUTING_KEY_DEBIT_WALLET = "wallet.debit"
ROUTING_KEY_DEPOSIT_WALLET = "wallet.deposit"
ROUTING_KEY_WALLET_MAINTENANCE = "wallet.deduction-fee"

# Authentication Exchange & Queues
AUTH_EXCHANGE = "auth.notifications"
EMAIL_VERIFICATION_QUEUE = "email.verification"
EMAIL_OTP_QUEUE = "email.otp"
EMAIL_RESET_PASSWORD_QUEUE = "email.reset-password"

ROUTING_KEY_EMAIL_VERIFICATION = "email.verification"
ROUTING_KEY_EMAIL_OTP = "email.otp"
ROUTING_KEY_EMAIL_RESET_PASSWORD = "email.reset-password"

def setup_rabbitmq():
    """Initialize RabbitMQ exchanges, queues, and bindings."""
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=os.getenv("RABBITMQ_HOST", "rabbitmq"))
    )
    channel = connection.channel()

    # Declare Exchanges
    channel.exchange_declare(exchange=WALLET_EXCHANGE, exchange_type="topic", durable=True)
    channel.exchange_declare(exchange=AUTH_EXCHANGE, exchange_type="direct", durable=True)

    # Declare Wallet Queues
    channel.queue_declare(queue=CREDIT_WALLET_QUEUE, durable=True)
    channel.queue_declare(queue=DEBIT_WALLET_QUEUE, durable=True)
    channel.queue_declare(queue=DEPOSIT_WALLET_QUEUE, durable=True)
    channel.queue_declare(queue=MAINTENANCE_DEDUCTION_QUEUE, durable=True)

    # Declare Authentication Queues
    channel.queue_declare(queue=EMAIL_VERIFICATION_QUEUE, durable=True)
    channel.queue_declare(queue=EMAIL_OTP_QUEUE, durable=True)
    channel.queue_declare(queue=EMAIL_RESET_PASSWORD_QUEUE, durable=True)

    # Bind Wallet Queues to Exchange
    channel.queue_bind(exchange=WALLET_EXCHANGE, queue=CREDIT_WALLET_QUEUE, routing_key=ROUTING_KEY_CREDIT_WALLET)
    channel.queue_bind(exchange=WALLET_EXCHANGE, queue=DEBIT_WALLET_QUEUE, routing_key=ROUTING_KEY_DEBIT_WALLET)
    channel.queue_bind(exchange=WALLET_EXCHANGE, queue=DEPOSIT_WALLET_QUEUE, routing_key=ROUTING_KEY_DEPOSIT_WALLET)
    channel.queue_bind(exchange=WALLET_EXCHANGE, queue=MAINTENANCE_DEDUCTION_QUEUE, routing_key=ROUTING_KEY_WALLET_MAINTENANCE)

    # Bind Authentication Queues to Exchange
    channel.queue_bind(exchange=AUTH_EXCHANGE, queue=EMAIL_VERIFICATION_QUEUE, routing_key=ROUTING_KEY_EMAIL_VERIFICATION)
    channel.queue_bind(exchange=AUTH_EXCHANGE, queue=EMAIL_OTP_QUEUE, routing_key=ROUTING_KEY_EMAIL_OTP)
    channel.queue_bind(exchange=AUTH_EXCHANGE, queue=EMAIL_RESET_PASSWORD_QUEUE, routing_key=ROUTING_KEY_EMAIL_RESET_PASSWORD)

    print(" [v] RabbitMQ Exchanges, Queues, and Bindings are set up.")

    connection.close()
