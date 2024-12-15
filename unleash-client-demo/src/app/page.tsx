'use client'
import dynamic from "next/dynamic";
import { FlagProvider } from "@unleash/proxy-client-react";
import { Todo } from "./Todo";
//import { TodoComponent } from "./TodoComponent";

const unleashConfig = {
    url: 'http://localhost:4242/api/frontend/',
    clientKey: 'default:development.unleash-insecure-frontend-api-token',
    appName: 'unleash-onboarding-react'
}

const FlagComponent = dynamic(() => import('./TextComponent').then((mod) => mod.TestComponent), {
    ssr: false,
});

const TodoComponent = dynamic(() => import('./TodoComponent').then((mod) => mod.TodoComponent), {
    ssr: false,
});

const todos: Todo[] = [
    {
        id: '1',
        title: 'Learn Next.js',
        description: 'Learn Next.js and build something great',
        dueDate: '2025-01-01'
    },
    {
        id: '2',
        title: 'Build something great',
        description: 'Learn Next.js and build something great',
        dueDate: '2025-01-02'
    }
]

export default function Home() {
  return (
      <FlagProvider config={unleashConfig}>
          <div className='container mx-auto p-4'>
              <h1 className='text-3xl font-bold mb-4'>Todo</h1>
              <ul className='list-disc'>
                  {todos.map(todo => (
                      <TodoComponent key={todo.id} {...todo} />
                  ))}
              </ul>
          </div>
      </FlagProvider>
  );
}
