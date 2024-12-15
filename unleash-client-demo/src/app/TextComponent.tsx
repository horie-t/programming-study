import { useEffect } from "react";
import { useFlag } from "@unleash/proxy-client-react";

export const TestComponent = () => {
    const hasVisited =
        typeof window !== "undefined" && !!localStorage.getItem("hasVisited");

    useEffect(() => {
        if (!hasVisited) localStorage.setItem("hasVisited", "true");
    }, []);

    const enabled = useFlag('support-due-date-time-frontend');

    return enabled ? <p>Flag is enabled</p> : <p>Flag is disabled</p>
};
