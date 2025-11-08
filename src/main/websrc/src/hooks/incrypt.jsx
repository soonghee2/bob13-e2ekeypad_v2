// // cryptoUtils.js

import JSEncrypt from 'jsencrypt';

export function encryptWithPublicKey(publicKey, data) {
    console.log("Using JSEncrypt for encryption");
    console.log("Public Key:", publicKey);
    console.log("Data to be encrypted:", data);
    // JSEncrypt 객체 생성 및 공개 키 설정
    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);

    // 데이터 암호화
    const encryptedData = encryptor.encrypt(data);

    if (encryptedData) {
        console.log("Data encrypted successfully:", encryptedData);
        return encryptedData;
    } else {
        console.error("Encryption failed. Possibly due to data size or key issue.");
        return null;
    }
}