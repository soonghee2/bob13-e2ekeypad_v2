import { useState, useEffect, useCallback } from 'react';
import { encryptWithPublicKey } from './incrypt'; // 함수 가져오기

export default function useSecureKeypad() {
    const [states, setStates] = useState({ keypad: null, userInput: '', keys: [] });
    const [clickedHashes, setClickedHashes] = useState('');
    const [clickCount, setClickCount] = useState(0);
    const [circleColors, setCircleColors] = useState(Array(6).fill('grey')); // 초기 색상은 회색


    useEffect(() => {
        const fetchImageAndKeys = async () => {
            try {
                const response = await fetch('/api/combined-image');
                const data = await response.json();
                if (data && data.imageBase64) {
                    setStates(prevStates => ({
                        ...prevStates,
                        keypad: `data:image/png;base64,${data.imageBase64}`,
                        keys: data.keys || [],
                        uuid: data.uuid,
                        hashedTimestamp: data.hashedTimestamp
                    }));
                }
            } catch (error) {
                console.error('Failed to fetch image and keys:', error);
            }
        };

        fetchImageAndKeys();
    }, []);

    const submitData = async (newHashes, uuid, hashedTimestamp) => {
        console.log('Submit data function called');
        try {
            const publicKey = `-----BEGIN PUBLIC KEY-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtkLA7dcyLqz4M6BS/XZiwMee85fjwskmxfZVN/qI854Sa4mlU/5Rse0HcNY0QoF+J3kQF3xWpTKLfw2p5pztsALLN6gsO2m4qLIOk3eNR+hVL2Rh4dc8MAhuXfoTGrfMjXouiy05rYgVpqIRRCjzMVGYnJ7arZ6rMN73nRxd0I9RVbe3LXEuHrBysxjfXae6z+qb+1Rp9MKnwiDuKC/i2lqqqmV9p/8OuY+qUzsMCtU8URS8kvw/bkg90TEOHzjKWrRIYRcQQkdJ8KuX3/lV1jBBgIQRfmQVTFUnkV5XBZw9jXYTsz6Bcp4MNWUlwHQIebAM8vMZ6/nH9p4OdETA5wIDAQAB
        -----END PUBLIC KEY-----`;

            const encryptedHashes = encryptWithPublicKey(publicKey, newHashes);
            console.log('Hashes:', newHashes);
            // 전송하기 전에 데이터를 출력
            console.log('Encrypted Hashes:', encryptedHashes);
            console.log('UUID:', uuid);
            console.log('Hashed Timestamp:', hashedTimestamp);
            const response = await fetch('/api/submit-hashes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    hashes: encryptedHashes,
                    uuid: uuid,
                    hashedTimestamp: hashedTimestamp,
                }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error('Failed to submit data');
            }
            const responseText = await response.text();
            alert(responseText);

            //const result = await response.json();
            // Optionally, handle success response here
        } catch (error) {
            console.error('Error submitting data:', error);
            // Optionally, handle error here
        }
    };

    const handleButtonClick = useCallback((index) => {
        const key = states.keys[index] || '';
        setClickedHashes(prevHashes => {
            const newHashes = prevHashes + key;
            console.log(`Button ${index + 1} pressed. Key: ${key}`);
            console.log(`Concatenated hashes so far: ${newHashes}`);
            console.log(newHashes.length);

            if (newHashes.length <= 240) { // 클릭 횟수가 6 이하일 때만 색상 변경
                setCircleColors(prevColors => {
                    const newColors = [...prevColors];
                    newColors[newHashes.length/40 - 1] = '#4eaeef';
                    return newColors;
                });
            }
            if (newHashes.length === 240) {
                //alert(`Concatenated hash: ${newHashes}`);
                // backend로 newHashes, uuid, hashedTimeStamp 보내기
                submitData(newHashes, states.uuid, states.hashedTimestamp);
                //window.location.reload();
                return 0;
            }
            return newHashes;
        });
    }, [states.keys]);

    return { states, handleButtonClick ,  circleColors};
}
