import {Title} from '@solidjs/meta'
import {announceRoutes} from '@solidjs/prerender'
import {Loading} from 'solid-js'
import {Router} from './router'
import './index.css'

export default function App() {
    announceRoutes(Router)

    return (
        <Router>
            {(props) => (
                <>
                    <Title>HAYO Industries</Title>
                    {/*<nav>*/}
                    {/*    <a href={paths()}>Home</a>*/}
                    {/*</nav>*/}
                    <Loading
                        // fallback={<main>Loading…</main>}
                    >{props.children}</Loading>
                </>
            )}
        </Router>
    )
}