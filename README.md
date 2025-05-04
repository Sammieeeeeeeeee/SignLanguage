# SignLanguageTranslator
Sign Language translator is an application that translates Malaysian Sign Language (MSL) using three different input methods:

## Features
1. **Image-to-Text Translation**
Users can upload an image of a hand gesture and the app uses a CNN model to classify and display the corresponding MSL sign.

2. **Video-to-Text Translation**
Users can upload video showing dynamic hand gestures. The app will extracts hand landmarks, performs similarity comparison using DTW (Dynamic Time Warping), and displays the recognized MSL sign.

3. **Text-to-Sign Translation**
Users can input any English word or phrase, and the app will retrieve corresponding MSL alphabets gestures from Firebase Storage and displays them.

## Model Code in Python
1. **CNN-based Model** for static sign recognition
2. **GRU-based Model** for dynamic sign recognition from videos
3. **DTW-based Matching** for gesture comparison and recognition

## Install the necessary libraries
- pip install -r requirements.txt

## Dependencies
1. **TensorFlow Lite**: for running Machine Learning models on-device
2. **MediaPipe**: for hand landmark extraction
3. **Firebase Storage**: for retrieving alphabet images

## Troubleshooting
1. **Emulator Compatibility**
Use emulator: Pixel 2 API 35 to run the application.



