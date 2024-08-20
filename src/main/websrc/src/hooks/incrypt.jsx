// // cryptoUtils.js
//
// export async function encryptWithPublicKey(publicKey, data) {
//     console.log("in encryptWithPublicKey");
//
//     try {
//         const encoder = new TextEncoder();
//         const encodedData = encoder.encode(data);
//         console.log("Encoded data:", encodedData);
//
//         // PEM 형식을 ArrayBuffer로 변환
//         const pemHeader = "-----BEGIN PUBLIC KEY-----";
//         const pemFooter = "-----END PUBLIC KEY-----";
//         const pemContents = publicKey.substring(pemHeader.length, publicKey.length - pemFooter.length).replace(/\s+/g, '');
//         console.log("PEM contents:", pemContents);
//
//         const binaryDerString = window.atob(pemContents);
//         const binaryDer = str2ab(binaryDerString);
//         console.log("Binary DER:", binaryDer);
//
//         // 공개 키를 가져옴
//         const key = await window.crypto.subtle.importKey(
//             "spki",
//             binaryDer,
//             {
//                 name: "RSA-OAEP",
//                 hash: "SHA-256"
//             },
//             true,
//             ["encrypt"]
//         );
//
//         console.log("Public key imported successfully:", key);
//
//         // 데이터를 암호화함
//         const encryptedData = await window.crypto.subtle.encrypt(
//             {
//                 name: "RSA-OAEP"
//             },
//             key,
//             encodedData
//         );
//
//         console.log("Data encrypted successfully:", encryptedData);
//
//         return window.btoa(String.fromCharCode.apply(null, new Uint8Array(encryptedData)));
//
//     } catch (error) {
//         console.error("Error during encryption:", error);
//     }
// }
//
// // str2ab 함수는 문자열을 ArrayBuffer로 변환합니다.
// function str2ab(str) {
//     const buf = new ArrayBuffer(str.length);
//     const bufView = new Uint8Array(buf);
//     for (let i = 0, strLen = str.length; i < strLen; i++) {
//         bufView[i] = str.charCodeAt(i);
//     }
//     return buf;
// }
//

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