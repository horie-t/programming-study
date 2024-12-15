import { useEffect } from "react";
import { useFlag } from "@unleash/proxy-client-react";
import { Todo } from "./Todo";

export const TodoComponent = (todo: Todo) => {
    const hasVisited =
        typeof window !== "undefined" && !!localStorage.getItem("hasVisited");

    useEffect(() => {
        if (!hasVisited) localStorage.setItem("hasVisited", "true");
    }, []);

    const enabled = useFlag('support-due-date-time-frontend');

    return (
        <li key={todo.id}>
            <h2 className='text-lg font-bold'>{todo.title}</h2>
            <p>{todo.description}</p>
            {enabled ? <p>{todo.dueDate}</p> : null}
        </li>
    )
};
