export class ObjectUtils {
    private constructor() {
    }

    static equals<T>(o1: T, o2: T) {
        if (typeof(o1) === 'string' || typeof(o1) === 'number' || typeof(o1) === "boolean") {
            return o1 === o2;
        }
        for (let k in o1) {
            if (!ObjectUtils.equals(o1[k], o2[k])) {
                return false;
            }
        }
        return true;
    }

    static clone<T>(obj: T): T {
        if (!obj) {
            return obj;
        }
        return JSON.parse(JSON.stringify(obj));
    }
}