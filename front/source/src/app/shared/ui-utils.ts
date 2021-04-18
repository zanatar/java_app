export class UIUtils {
    private static readonly SMALL_WIDTH = 768;

    static hasEnoughWidth(width: number): boolean {
        return width >= UIUtils.SMALL_WIDTH;
    }
}
