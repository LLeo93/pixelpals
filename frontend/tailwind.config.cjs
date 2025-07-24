/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        oxanium: ['Oxanium', 'sans-serif'],

        inter: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
