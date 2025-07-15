import React from 'react';

const Footer = () => {
  return (
    <footer className="bg-gray-800 p-6 text-center text-gray-400 text-sm shadow-inner">
      <div className="container mx-auto">
        <p>
          &copy; {new Date().getFullYear()} PixelPals. Tutti i diritti
          riservati.
        </p>
        <div className="mt-2 space-x-4">
          <a href="#" className="hover:text-white transition duration-300">
            Privacy Policy
          </a>
          <a href="#" className="hover:text-white transition duration-300">
            Termini di Servizio
          </a>
          <a href="#" className="hover:text-white transition duration-300">
            Contatti
          </a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
