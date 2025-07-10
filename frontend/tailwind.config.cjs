/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      // Estendi la configurazione dei font di Tailwind CSS
      fontFamily: {
        // Definisci il font 'Oxanium' per i titoli e gli elementi di spicco
        oxanium: ['Oxanium', 'sans-serif'],
        // Definisci il font 'Inter' come font di base o per il testo del corpo
        inter: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
