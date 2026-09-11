import {fileRoutes} from 'filesystem-routing/vite';
import solid from '@solidjs/vite-plugin';
import {prerender} from 'prerender-crawler/vite';
import {serverFunctions} from '@solidjs/prerender/integration';
import {defineConfig} from "vite";

// https://github.com/solidjs/templates/blob/3f45de8d6672d0998f295a9a9423909c7e36b021/solid-v2/ssg/vite.config.ts
export default defineConfig({
    base: '/hayo-industries/',
    plugins: [
        solid({
            start: true,
            ssr: true,
            serverFunctions: true,
            diagnostics: true,
            extensions: ['.jsx', '.tsx'],
        }),
        fileRoutes({types: true}),
        prerender({
            mode: 'static', integrations: [serverFunctions()]
        }),
    ],
    server: {
        port: 3000,
    },
    build: {
        target: 'esnext',
        assetsInlineLimit: 0,
    },
})