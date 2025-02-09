from celery import Celery
from app import create_app  
from config import Config


# Create the Flask app
app = create_app()

def make_celery(app):
    celery = Celery(app.import_name, broker=Config.CELERY_BROKER_URL)
    celery.conf.update(app.config)
    return celery


celery = make_celery(app)
