from flask import Flask
from flask_mail import Mail
import os

# Initialize Flask app with template folder
app = Flask(__name__, template_folder="templates")


# Load configuration from config.py
config_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'config.py')
if os.path.exists(config_path):
    app.config.from_pyfile(config_path)
else:
    raise FileNotFoundError(f"Configuration file not found: {config_path}")

# Initialize Flask-Mail
mail = Mail(app)

# Import routes after the app is created to avoid circular imports
from .routes import *

def start_consumer():
    """Start the RabbitMQ consumer in a separate thread."""
    from .consumer import Consumer 
    consumer = Consumer()
    consumer.start_consuming()


# Start the consumer when the app starts
if __name__ == "__main__":
    import threading
    consumer_thread = threading.Thread(target=start_consumer)
    consumer_thread.daemon = True
    consumer_thread.start()