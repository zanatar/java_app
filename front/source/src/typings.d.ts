declare var module: NodeModule;

interface NodeModule {
    id: string;
}

declare namespace VK {
    class Auth {
        static login(callback: (response: {session: any, status: string}) => void, settings: number);
    }
}
