from flask import Blueprint
from .controller.notification_controller import (
    send_verification_email,
    send_otp_email,
    send_reset_password_email,
)

# Create a Blueprint for notifications
notification_bp = Blueprint("notification", __name__)

# Define routes
notification_bp.route("/send-verification-email", methods=["POST"])(send_verification_email)
notification_bp.route("/send-otp-email", methods=["POST"])(send_otp_email)
notification_bp.route("/send-reset-password-email", methods=["POST"])(send_reset_password_email)