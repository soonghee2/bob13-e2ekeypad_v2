import { useState, useEffect, useCallback } from 'react';
import { encryptWithPublicKey } from './incrypt'; // 함수 가져오기

export default function useSecureKeypad() {
    const [states, setStates] = useState({ keypad: null, userInput: '', keys: [] });
    const [clickedHashes, setClickedHashes] = useState('');
    const [clickCount, setClickCount] = useState(0);
    const [circleColors, setCircleColors] = useState(Array(6).fill('grey')); // 초기 색상은 회색
    const [publicKey, setPublicKey] = useState('');
    
    const [isLoading, setIsLoading] = useState(false);

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

    useEffect(() => {
        const fetchPublicKey = async () => {
            try {
                const response = await fetch('/public.pem');
                if (!response.ok) {
                    throw new Error('Failed to fetch public key');
                }
                const keyText = await response.text();
                setPublicKey(keyText);
            } catch (error) {
                console.error('Failed to load public key:', error);
            }
        };

        fetchPublicKey();
    }, []);

    const submitData = useCallback(async (newHashes, uuid, hashedTimestamp) => {
        console.log('Submit data function called');
        setIsLoading(true);  
        try {
            if (!publicKey) {
                throw new Error('Public key is not loaded yet');
            }

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
            return responseText;

            //const result = await response.json();
            // Optionally, handle success response here
        } catch (error) {
            console.error('Error submitting data:', error);
            throw error;
        } finally {
            setIsLoading(false);    // ====== 로딩 END
        }
    }, [publicKey]);

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
                submitData(newHashes, states.uuid, states.hashedTimestamp)
                    .then(() => {
                        window.location.reload()
                    })
                    .catch((error) => {
                        console.error('Submit error received, reloading anyway:', error);

                        window.location.reload(true);
                    });
                return '';
            }
            return newHashes;
        });
    }, [states.keys, submitData]);

    return { states, handleButtonClick ,  circleColors, isLoading};
}
