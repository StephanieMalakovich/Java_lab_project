package functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable {

    private Node head;

    private static class Node{
        Node prev, next;
        double x, y;

        Node() {
            this.x = 0;
            this.y = 0;
            this.prev = null;
            this.next = null;
        }

        Node(double x, double y) {
            this.x = x;
            this.y = y;
            this.next = null;
            this.prev = null;
        }
    }

    private void addNode(double x, double y){

        if(head!=null){

            // Указателем проходимся по всему списку
            Node Pointer = head.next;
            while(Pointer.next!=head){Pointer = Pointer.next;}

            // Запоминаем последний элемент списка и создаём новый
            Node prevPointer = Pointer;
            Pointer.next = new Node(x, y);

            Pointer = Pointer.next;

            // Вставляем элемент в список
            Pointer.prev = prevPointer;
            Pointer.next = head;
            head.prev = Pointer;

        } else{
            head = new Node(x, y);
            head.prev = head;
            head.next = head;
        }
        ++count;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {

        for (int i = 0; i < xValues.length - 1; i++) {
            for (int j = i+1; j < xValues.length; j++) {
                if(xValues[i] == xValues[j]) { throw new IllegalArgumentException("The array must be without duplicates");}
            }
            if(xValues[i] > xValues[i+1]){ throw new IllegalArgumentException("The array should be sorted");}
        }

        if (xValues.length != yValues.length) { throw new IllegalArgumentException("The sizes of the arrays must be the same");}

        for (int i = 0; i < xValues.length; i++) { this.addNode(xValues[i], yValues[i]);}
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if(xFrom>xTo){
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        head = new Node();
        Node pointer = head;

        // Прим. если xTo = xFrom, то xDelta = 0
        double xDelta = (xTo - xFrom) / count;

        for (int i = 0; i < count; i++) {
            pointer.x = xFrom + i * xDelta;
            pointer.y = source.apply(pointer.x);
            pointer.next = new Node();
            pointer.next.prev = pointer;
            pointer = pointer.next;
        }

        pointer.x = xFrom + count * xDelta;
        pointer.y = source.apply(pointer.x);
        pointer.next = head;
        head.prev = pointer;

    }

    private Node getNode(int index){

        Node pointer = head;

        if(index < count/2) {
            for (int i = 0; i < index; i++) { pointer = pointer.next; }
        } else{
            for (int i = count; i > index; i--) { pointer = pointer.prev; }
        }

        return pointer;
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public double getX(int index){
        return getNode(index).x;
    }
    @Override
    public double getY(int index){
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value){
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x){
        Node pointer = head;
        for (int i = 0; i < count; i++) {
            if (pointer.x == x){ return i;}
            pointer = pointer.next;
        }
        return -1;
    }
    @Override
    public int indexOfY(double y){
        Node pointer = head;
        for (int i = 0; i < count; i++) {
            if (pointer.y == y){ return i;}
            pointer = pointer.next;
        }
        return -1;
    }

    @Override
    public double leftBound(){
        return head.x;
    }
    @Override
    public double rightBound(){
        return head.prev.x;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < head.x) {
            return 0;
        }
        Node pointer = head.next;
        for (int i = 1; i < count; i++) {
            if (x < pointer.x) { return i - 1; }
        }
        return count;
    }

    protected Node floorNodeOfX(double x) {
        if (x < head.x) {
            return head;
        }
        Node pointer = head;
        for (int i = 0; i < count; i++) {
            if (x < pointer.next.x) { return pointer; }
            pointer = pointer.next;
        }
        return head.prev;
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        Node pointer = head;
        for (int i = 0; i <= floorIndex; i++) {pointer = pointer.next;}

        return interpolate(x, pointer.x, pointer.next.x, pointer.y, pointer.next.y);
    }

    protected double interpolate(double x, Node floorNode) {
        return interpolate(x, floorNode.x, floorNode.next.x, floorNode.y, floorNode.next.y);
    }

    @Override
    protected double extrapolateLeft(double x) {
        if(count == 1) {
            return (head.y);
        }
        return interpolate(x, head.x, head.next.x, head.y, head.next.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        if(count == 1) {
            return (head.y);
        }
        return interpolate(x, head.prev.prev.x, head.prev.x, head.prev.prev.y, head.prev.y);
    }

    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int index = indexOfX(x);
            if (index != -1) {
                return getY(index);
            } else {
                return interpolate(x, floorNodeOfX(x));
            }
        }
    }
    @Override
    public void insert(double x, double y) {
        if (head == null) {
            addNode(x, y);
            return;
        }

        if (x < head.x) {
            Node newNode = new Node(x, y);
            newNode.next = head;
            newNode.prev = head.prev;
            head.prev.next = newNode;
            head.prev = newNode;
            head = newNode;
            count++;
            return;
        }

        Node pointer = head;
        while (pointer.next != head && pointer.next.x < x) {
            pointer = pointer.next;
        }

        if (pointer.x == x) {
            pointer.y = y;
            return;
        }
        if (pointer.next.x == x) {
            pointer.next.y = y;
            return;
        }

        Node newNode = new Node(x, y);
        newNode.next = pointer.next;
        newNode.prev = pointer;
        pointer.next.prev = newNode;
        pointer.next = newNode;
        count++;
    }
}