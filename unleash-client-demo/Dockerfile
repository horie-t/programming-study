FROM node:18-alpine

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY src ./src
COPY public ./public
COPY next.config.ts .
COPY tsconfig.json .
COPY eslint.config.mjs .
COPY tailwind.config.ts .
COPY postcss.config.mjs .

CMD npm run dev