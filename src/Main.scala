
object HelloWorld {
    def main(args: Array[String]) {
      
      val one = new Node("1.1.1.1",10111,"user1")
      val two = new Node("2.2.2.2",10111,"user2")
      val three = new Node("3.3.3.3",10111,"user3")
      val group = new Group("test")
      group.members.add(one)
      group.members.add(two)
      group.members.add(three)
      one.groupList.add(group)
      two.groupList.add(group)
      three.groupList.add(group)
    }
}