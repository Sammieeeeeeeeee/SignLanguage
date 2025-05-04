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

## Datasets

- **CNN Dataset**: [Access the CNN Dataset here](https://drive.google.com/drive/folders/1bDFKsnwbNyyveFrnrabuRmMOO3ikIx93?usp=sharing)
  
- **DTW Dataset**: [Access the DTW Dataset here](https://drive.google.com/drive/folders/1WH-gdkaYAcMegYFwvTkFokYEY3rg4W4H?usp=sharing)

- **GRU Dataset**: [Access the GRU Dataset here](https://drive.google.com/drive/folders/1IA_les7aDuGbBYg0l3Zx9FGZl0QyMPME?usp=sharing)

## References

- The GRU Python code in this project is based on the work from the following repository:
  [Sign Language Recognition - MediaPipe-DTW](https://github.com/gabguerin/Sign-Language-Recognition--MediaPipe-DTW/tree/master)

- The CNN Python code is adapted from the following Kaggle notebook:
  [CNN with MediaPipe for Sign Language Recognition](https://www.kaggle.com/code/mlanangafkaar/cnn-with-mediapipe-for-sign-language-recognition/notebook)




