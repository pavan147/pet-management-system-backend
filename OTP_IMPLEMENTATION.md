# OTP Implementation (Short Notes)

This is a minimal OTP flow added to the backend using the existing `Owner` table and phone number.

## 1) What was added

- OTP APIs:
  - `POST /api/otp/send`
  - `POST /api/otp/verify`
- OTP service:
  - Generate 6-digit OTP
  - Save OTP + expiry in `Owner`
  - Verify OTP by phone number
- Optional Telegram notification for OTP send
- Security update to allow OTP APIs without login

## 2) Files changed

### Updated
- `src/main/java/com/pet/manage/system/entity/Owner.java`
  - added fields:
    - `otpCode`
    - `otpExpiresAt`
    - `phoneVerified`
- `src/main/java/com/pet/manage/system/commons/Constants.java`
  - added public URL: `"/api/otp/**"`
- `src/main/resources/application.properties`
  - added:
    - `app.otp.expiry-minutes=5`
    - `telegram.bot-token=`
    - `telegram.chat-id=`

### New
- `src/main/java/com/pet/manage/system/controller/OtpController.java`
- `src/main/java/com/pet/manage/system/service/OtpService.java`
- `src/main/java/com/pet/manage/system/service/Impl/OtpServiceImpl.java`
- `src/main/java/com/pet/manage/system/dtos/request/SendOtpRequestDTO.java`
- `src/main/java/com/pet/manage/system/dtos/request/VerifyOtpRequestDTO.java`

## 3) How it works

### Send OTP
1. API receives phone number.
2. Backend finds owner by phone number from `Owner` table.
3. Generates OTP (6 digits).
4. Stores OTP and expiry time in same owner row.
5. Sends OTP text to Telegram (if bot config present).

### Verify OTP
1. API receives phone number + OTP.
2. Backend finds owner by phone number.
3. Checks OTP exists and not expired.
4. If valid:
   - sets `phoneVerified = true`
   - clears OTP fields

## 4) API request samples

### `POST /api/otp/send`
```json
{
  "phoneNumber": "9579967444"
}
```

### `POST /api/otp/verify`
```json
{
  "phoneNumber": "9579967444",
  "otp": "123456"
}
```

## 5) Important notes

- OTP is currently stored in plain text in DB (minimal implementation).
- Telegram is optional; if token/chat-id are empty, no Telegram message is sent.
- OTP routes are public because they are in `Constants.PUBLIC_URLS`.

## 6) Quick check points

- Phone number must already exist in `Owner` table.
- `spring.jpa.hibernate.ddl-auto=update` should create new owner columns.
- Set Telegram values in `application.properties` if you want OTP alerts.

