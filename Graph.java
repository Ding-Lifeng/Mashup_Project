import java.util.*;

class Graph  //图-邻接矩阵存储
{
    private class VNode{  //图结点类
        final private String api_name;
        private ArrayList<String> categories;  //使用contains进行元素存在判断
        private int count_categories;
        private HashSet<Edge> edges;

        public VNode(String api_name) {
            this.api_name = api_name;
            this.categories = new ArrayList<>();
            this.count_categories = 0;
            this.edges = new HashSet<>();
        }

        public boolean containsCategory(String category){
            return categories.contains(category);
        }

        public boolean hasEdges(){return !edges.isEmpty();}

        public boolean containsEdge(Edge e){
            return edges.contains(e);
        }

        public void addCategory(String category){
            if(category.isEmpty() || categories.contains(category))
                return;
            categories.add(category);
        }

        public void addEdge(Edge e){  //添加权值
            if(containsEdge(e)){
                for(Edge edge:edges)
                    if(edge.equals(e))
                        edge.add_weight();
            }
            else
                edges.add(e);  //添加新边
        }

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder("VNode{" + "api_name='" + api_name + '\'' + ", categories=");
            for(String category:categories) {
                output.append(category).append(",");
            }
            output.append("[Edges]");
            for(Edge e:edges){
                output.append(e).append(",");
            }
            return output.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return this.api_name.equals(((VNode) o).api_name);
        }
    }

    private class Edge implements Comparable<Edge>  //定义无向图的边
    {
        private String head;
        private String tail;
        private int weight;  //权重

        public Edge(String head, String tail) {
            this.head = head;
            this.tail = tail;
            this.weight = 1;  //边权初始化
        }

        public void add_weight()
        {
            this.weight++;
        }

        public String getHead() {
            return this.head;
        }

        public String getTail(){
            return this.tail;
        }

        @Override
        public boolean equals(Object o)  //边的比较
        {
            if(this==o)
                return true;
            if(o==null || getClass() != o.getClass())
                return false;
            Edge edge = (Edge) o;
            return Objects.equals(this.head,edge.head) && Objects.equals(this.tail,edge.tail);
        }

        @Override
        public int compareTo(Edge edge){  //根据权值排序
            return this.weight- edge.weight;
        }

        @Override
        public String toString() {
            return "Edge{" +
                    "head='" + head + '\'' +
                    ", tail='" + tail + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }

    private TreeMap<String,VNode> graph;  //使用TreeMap顺序保存节点
    private VNode node;

    public Graph(){
        this.graph = new TreeMap<>();
    }

    public  int node_num(){  //返回结点数
        return graph.size();
    }

    public boolean contains_node(String api_name){
        return graph.containsKey(api_name);
    }

    //获得图的全部Key值
    public Set<String> getAllNodeKeys(){
        return graph.keySet();
    }

    // 得到Api对应的categories
    public ArrayList<String> get_Node_Categories(String api_name)
    {
        if(contains_node(api_name)) {
            node = graph.get(api_name);
            return node.categories;
        }
        return null;
    }

    // 得到Api对应的特征数
    public Integer get_Node_categories_count(String api_name)
    {
        if(contains_node(api_name)) {
            node = graph.get(api_name);
            return node.count_categories;
        }
        return null;
    }

    // 将图中所有的边按照头结点+" "+尾结点+" "+权值的相反数的格式输出
    public void printAllEdges(){
        for(VNode v: graph.values())
            for(Edge e: v.edges){
                e.weight = -e.weight;
                System.out.println(e.getHead() + "," + e.getTail() + "," +e.weight);
            }
    }


    // 遍历图中所有结点，返回categories中包含target特征的Api_name组成的数组
    public ArrayList<String> search_pro_Node(String[] target)
    {
        ArrayList<String> result = new ArrayList<>();  // 存储潜在结点
        for(Map.Entry<String, VNode> entry: graph.entrySet())
        {
            node = entry.getValue();
            for (String s : target) {
                if (node.categories.contains(s))
                {
                    node.count_categories++;
                    if(!result.contains(node.api_name))
                        result.add(node.api_name);
                }
            }
        }
        return result;
    }

    // 选择关键结点
    public Set<String> select_critical_apis(ArrayList<String> pro_apis, String[] target)
    {
        Set<String> result = null;
        int max = 0;  // 记录具有最多标签的单个结点
        // 根据结点的特征数量-进行潜在结点分组
        ArrayList<ArrayList<String>> pro_api_group = new ArrayList<>();
        int i = 0;
        while(i<=20)  //单个结点最多具有20个categories
        {
            pro_api_group.add(new ArrayList<>());
            i++;
        }
        for(String point: pro_apis)  //分组
        {
            node = graph.get(point);
            if(node.count_categories>20)
            {
                System.out.println("单个结点具有20个以上的标签");
                return null;
            }
            if(node.count_categories>max)
                max = node.count_categories;
            pro_api_group.get(node.count_categories).add(point);
        }

        // 通过组合确定关键结点
        for(i = max;i > 0;i--)  //循环查找满足标签的组合，具有最多标签的结点优先
        {
            for(String point:pro_api_group.get(i)) {
                result = combine_apis(point,pro_api_group,i,target);
                if(result != null)
                    return result;
            }
        }
        return null;  //返回查找失败
    }

    //查找潜在结点中和Api来满足查找条件(原则-最少关键结点数)
    private VNode combine_node;
    public Set<String> combine_apis(String point,ArrayList<ArrayList<String>> pro_apis, int categories, String[] target)
    {
        Set<String> result = new HashSet<String>();
        int[] categories_tag = new int[target.length];  //标记位，目前的Apis组合覆盖的标签
        for(int n=0; n<target.length; n++)
        {
            combine_node = graph.get(point);
            if(node.containsCategory(target[n]))
                categories_tag[n] = 1;
        }
        result.add(point);
        for(int j = categories; j>0; j--)
        {
            for(String combine : pro_apis.get(j))
            {
                if(update_tag(combine,categories_tag,target))
                    result.add(combine);
            }
            if(is_Finished(categories_tag))
                return result;
        }
        if(is_Finished(categories_tag))
            return result;
        else
            return null;
    }

    public boolean update_tag(String combine, int[] categories_tag, String[] target)
    {
        boolean tag = false;
        //判断加入的结点是否更新了categories
        VNode update_node = graph.get(combine);
        for(int i=0; i< target.length;i++)
        {
            if(update_node.containsCategory(target[i])){
                if(categories_tag[i] == 0)
                {
                    categories_tag[i] = 1;
                    tag = true;
                }
            }
        }
        return tag;
    }

    //判断当前的Api组合是否满足条件
    public boolean is_Finished(int[] categories_tag)
    {
        for(int tag:categories_tag)
        {
            if(tag == 0)
                return false;
        }
        return true;
    }

    public void addEdge(String head, String tail){
        if(contains_node(head) && contains_node(tail)){
            graph.get(head).addEdge(new Edge(head, tail));
            graph.get(tail).addEdge(new Edge(tail, head));
        }
    }

    public void clearVNode(){  //移除无边结点
        HashSet<String> useless_node = new HashSet<>();
        for(VNode v : graph.values()){
            if(!v.hasEdges()){
                useless_node.add(v.api_name);
            }
        }
        for(String name : useless_node){
            graph.remove(name);
        }
    }

    public void addVNode(String api_name, LinkedList<String> categories){
        VNode v = new VNode(api_name);
        for (String category : categories) {
            v.addCategory(category);
        }
        this.graph.put(api_name, v);
    }

    public String get_info(int k){
        int cnt = 0;
        for(VNode v : graph.values()){
            if(v.hasEdges()){
                cnt++;
            }
        }
        return node_num() + " " + cnt + " " + k;  //点数、边数、关键结点数
    }
}
