export class ProtoMsg {
    contentType
    business
    from
    target
    content

    constructor(contentType, business, from, target, content) {
        this.contentType = contentType
        this.business = business
        this.from = from
        this.target = target
        this.content = content
    }
}
