import type { components } from "@/src/lib/backend/apiV1/schema";

type PostDto = components["schemas"]["PostDto"];

type PostItemPageDto = components["schemas"]["PageDtoPostDto"];

export default async function Page({
    searchParams,
}: {
    searchParams: {
        searchKeywordType?: "title" | "content";
        searchKeyword?: string;
    }
}) {

    const { searchKeyword = "", searchKeywordType = "title" } = searchParams;
    const respone = await fetch("http://localhost:8080/api/v1/posts")
    const body: PostItemPageDto = await respone.json();

    return <div>
        <form>
            <select name="searchKeywordType">
                <option value="title">제목</option>
                <option value="content">내용</option>
            </select>
            <input type="text" name="searchKeyword" />
            <button type="submit">검색</button>
        </form>

        <div>
            currentPageNumber: {body.currentPageNumber}
        </div>

        <div>
            pageSize: {body.pageSize}
        </div>

        <div>
            totalPages: {body.totalPages}
        </div>

        <div>totalItems: {body.totalItems}</div>

        <hr />

        <ul>
            {body.items?.map((item: PostDto) => (
                <li key={item.id} className="border-[2px] border-[red] my-3">
                    <div>id: {item.id}</div>
                    <div>createDate : {item.createDate}</div>
                    <div>modifyDate : {item.modifyDate}</div>
                    <div>authorId : {item.authorId}</div>
                    <div>modiauthorNameyDate : {item.authorName}</div>
                    <div>title : {item.title}</div>
                    <div>published : {`${item.published}`}</div>
                    <div>listed : {`${item.listed}`}</div>
                </li>
            ))}
        </ul>
    </div>;
}