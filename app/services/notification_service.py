from flask_mail import Message
from flask import render_template
from . import mail

class NotificationService:
    @staticmethod
    def send_email(to, subject, template, **kwargs):
        print(f" [x] Preparing to send email to {
              to} with subject: {subject}")  # Debug log
        try:
            msg = Message(subject, recipients=[to])
            msg.body = render_template(template + '.txt', **kwargs)
            msg.html = render_template(template + '.html', **kwargs)
            mail.send(msg)
            print(f" [✓] Email successfully sent to {to}")  # Success log
        except Exception as e:
            print(f" [!] Failed to send email: {e}")  # Error log

    @staticmethod
    def send_verification_email(to, username, verification_link):
        print(f" [x] Sending verification email to {to}")  # Debug log
        NotificationService.send_email(
            to=to,
            subject="Email Verification",
            template="verification_email",
            username=username,
            verification_link=verification_link
        )

    @staticmethod
    def send_otp_email(to, username, otp):
        print(f" [x] Sending OTP email to {to}")  # Debug log
        NotificationService.send_email(
            to=to,
            subject="OTP Verification",
            template="otp_email",
            username=username,
            otp=otp
        )

    @staticmethod
    def send_reset_password_email(to, username, reset_link):
        print(f" [x] Sending reset password email to {to}")  # Debug log
        NotificationService.send_email(
            to=to,
            subject="Password Reset",
            template="reset_password_email",
            username=username,
            reset_link=reset_link
        )
