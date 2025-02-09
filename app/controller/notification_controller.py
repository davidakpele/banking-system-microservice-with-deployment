from flask import request, jsonify
from app import app
from app.producer import Producer

@app.route('/send-verification-email', methods=['POST'])
def send_verification_email():
    data = request.json
    producer = Producer()
    producer.send_verification_email(data['email'], data['username'], data['verification_link'])
    producer.close()
    return jsonify({"message": "Verification email sent"}), 200

@app.route('/send-otp-email', methods=['POST'])
def send_otp_email():
    data = request.json
    producer = Producer()
    producer.send_otp_email(data['email'], data['username'], data['otp'])
    producer.close()
    return jsonify({"message": "OTP email sent"}), 200

@app.route('/send-reset-password-email', methods=['POST'])
def send_reset_password_email():
    data = request.json
    producer = Producer()
    producer.send_reset_password_email(data['email'], data['username'], data['reset_link'])
    producer.close()
    return jsonify({"message": "Reset password email sent"}), 200