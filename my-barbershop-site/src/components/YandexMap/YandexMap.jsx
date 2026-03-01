import React, { useEffect, useRef } from 'react';
import styles from './YandexMap.module.css';

const barbershopLocation = [53.917914, 27.558085]; 

export default function YandexMap() { 
  const mapRef = useRef(null); 

  useEffect(() => {
    let myMapInstance = null; 
    if (typeof window.ymaps !== 'undefined' && mapRef.current) { 
        window.ymaps.ready(() => {
            console.log('Yandex Maps API ready, initializing map...');

            const mapContainer = mapRef.current; 

            if (!mapContainer) return; 

            const mapState = {
                center: barbershopLocation, 
                zoom: 16, 
                controls: ['zoomControl', 'fullscreenControl']
            };

            const mapOptions = {
                searchControlProvider: 'yandex#search'
            };

            myMapInstance = new window.ymaps.Map(mapContainer, mapState, mapOptions);

            const placemark = new window.ymaps.Placemark(barbershopLocation, {
                hintContent: 'Салон красоты CutLoose',
                balloonContent: 'Добро пожаловать в наш салон красоты!'
            });

            myMapInstance.geoObjects.add(placemark);

        });
    } else {
      console.log('Yandex Maps API not yet loaded or container not available.');
    }

    return () => {
         console.log('Cleaning up Yandex map effect...');
         if (myMapInstance) {
             myMapInstance.destroy(); 
             console.log('Yandex map instance destroyed.');
         }
    };

  }, []); 

  return (
    <div ref={mapRef} className={styles.yandexMapContainer}>
    </div>
  );
}