import React, {useEffect, useState} from "react";

export const ImageLoader = ({src, ...props}) => {

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<boolean>(false);

    useEffect(() => {
        setLoading(true);
        setError(false);

        const imgInstance = new Image();
        imgInstance.src = src;
        
        imgInstance.onload = () => {
            setLoading(false);
            setError(false);
        };

        imgInstance.onerror = () => {
            setLoading(false);
            setError(true);
        };

    }, [src])
    
    // loading && !error : loading page
    // error: err page
    // !loading && !error successfully loaded
    return (
        <>
            {loading && !error ? 
                <img src="loading.png" 
                    className={`flex-none animate-pulse bg-gray-200 ${props.className}`}></img> 
                : null}
            {error ? <img src="error-404.png" className={props.className}></img> : null}
            {!loading && !error ? <img src={src} className={props.className}></img> : null}
        </>
    )
}