import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPhone, faEnvelope } from '@fortawesome/free-solid-svg-icons';

const Footer = () => {
  return (
    <footer className="bg-gray-800 p-5 text-center text-gray-400 text-sm shadow-inner">
      <div className="container mx-auto max-w-6xl">
        <p className="mb-2">
          &copy; {new Date().getFullYear()} PixelPals. Tutti i diritti
          riservati.
        </p>
        <div className="flex flex-col md:flex-row justify-center items-center space-y-1 md:space-y-0 md:space-x-4 mb-2">
          <a
            href="tel:+393806952354"
            className="hover:text-white transition duration-300 flex items-center"
          >
            <FontAwesomeIcon icon={faPhone} className="mr-2 text-purple-400" />
            +39 380 6952354
          </a>
          <a
            href="mailto:liba.leoncini@gmail.com"
            className="hover:text-white transition duration-300 flex items-center"
          >
            <FontAwesomeIcon icon={faEnvelope} className="mr-2 text-blue-400" />
            liba.leoncini@gmail.com
          </a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
