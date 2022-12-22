<template>
  <div>
    <el-button @click="batchDelete" size="mini" type="danger" >批量删除</el-button>
    <el-switch v-model="draggable" active-text="开启拖拽" inactive-text="关闭拖拽">
    </el-switch>
    <el-button @click="batchSave" size="mini" v-if="draggable">批量保存</el-button>
    <el-tree
      :data="categoryList"
      :props="defaultProps"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
      ref="categoryTree"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button v-if="node.level <= 2" type="text" size="mini" @click.stop="() => append(data)">
            Append
          </el-button>
          <el-button type="text" size="mini" @click.stop="() => edit(data)">
            Edit
          </el-button>
          <el-button v-if="node.childNodes.length === 0" type="text" size="mini" @click.stop="() => remove(node, data)">
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog :title="title" :visible.sync="dialogVisible" width="30%" :close-on-click-modal="false">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
// 例如：import 《组件名称》 from ‘《组件路径》‘;

export default {
  name: 'category',
  // import引入的组件需要注入到对象中才能使用
  components: {},
  data () {
    return {
      pCid: [],
      draggable: false,
      updateNodes: [],
      maxLevel: 0,
      title: '',
      dialogType: '', // edit,add
      categoryList: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      expandedKey: [],

      dialogVisible: false,
      category: {
        name: '',
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        icon: '',
        productUnit: '',
        catId: null
      }
    }
  },
  // 方法集合
  methods: {
    handleNodeClick (nodeInfo) {
      console.log('tree->@node-click', nodeInfo)
    },
    // 获取所有分类信息
    getMenus () {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(response => {
        this.categoryList = response.data.data
      })
    },
    // 新增分类
    append (data) {
      console.log('append----', data)
      this.dialogType = 'add'
      this.title = '添加分类'
      this.category.parentCid = data.catId
      this.category.catLevel = data.catLevel * 1 + 1
      this.category.catId = null
      this.category.name = null
      this.category.icon = ''
      this.category.productUnit = ''
      this.category.sort = 0
      this.category.showStatus = 1
      this.dialogVisible = true
    },
    // 添加三级分类
    addCategory () {
      console.log('提交的三级分类数据', this.category)
      this.$http({
        url: this.$http.adornUrl('/product/category/save'),
        method: 'post',
        data: this.$http.adornData(this.category, false)
      }).then(({ data }) => {
        this.$message.success('分类添加成功')
        this.dialogVisible = false
        // 刷新出新的菜单
        this.getMenus()
        this.expandedKey = [this.category.parentCid]
      }).catch(() => {})
    },
    // 修改分类
    edit (data) {
      console.log('要修改的数据-->', data)
      this.dialogType = 'edit'
      this.title = '修改分类'
      // 发送请求获取节点最新的数据
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: 'get'
      }).then(({ data }) => {
        // 请求成功
        console.log('要回显的数据-->', data)
        this.category.name = data.data.name
        this.category.catId = data.data.catId
        this.category.icon = data.data.icon
        this.category.productUnit = data.data.productUnit
        this.category.parentCid = data.data.parentCid
        this.dialogVisible = true
      })
    },
    // 修改三级分类数据
    editCategory () {
      const {catId, name, icon, productUnit} = this.category
      this.$http({
        url: this.$http.adornUrl('/product/category/update'),
        method: 'put',
        data: this.$http.adornData({ catId, name, icon, productUnit }, false)
      })
        .then(({ data }) => {
          this.$message.success('菜单修改成功!')
          // 关闭对话框
          this.dialogVisible = false
          // 刷新出新的菜单
          this.getMenus()
          // 设置需要默认展开的菜单
          this.expandedKey = [this.category.parentCid]
        })
        .catch(() => {})
    },
    // 提交模态框
    submitData () {
      if (this.dialogType === 'add') {
        this.addCategory()
      }
      if (this.dialogType === 'edit') {
        this.editCategory()
      }
    },
    // 删除分类
    remove (node, data) {
      console.log('remove---', node)
      console.log('data---', data)
      const ids = [data.catId]

      this.$confirm(`是否删除【${data.name}】当前菜单?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'delete',
          data: this.$http.adornData(ids, false)
        }).then(({ data }) => {
          this.$message.success('菜单删除成功')
          // 刷新出新的菜单
          this.getMenus()
          this.expandedKey = [node.parent.data.catId]
        }).catch(() => {})
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 批量保存最终拖拽数据
    batchSave () {
      console.log(this.updateNodes)
      this.$http({
        url: this.$http.adornUrl('/product/category/update/sort'),
        method: 'put',
        data: this.$http.adornData(this.updateNodes, false)
      })
        .then(({ data }) => {
          this.$message.success('菜单顺序修改成功!')
          // 刷新出新的菜单
          this.getMenus()
          // 设置需要默认展开的菜单
          this.expandedKey = this.pCid
          this.updateNodes = []
          this.maxLevel = 0
          // this.pCid = 0;
        })
        .catch(() => {})
    },
    // 拖拽成功完成时触发的事件
    handleDrop (draggingNode, dropNode, dropType, ev) {
      console.log('handleDrop: ', draggingNode, dropNode, dropType)

      // 当前节点最新的父节点
      let pCid = 0
      // 节点的顺序
      let siblings = null
      if (dropType === 'before' || dropType === 'after') {
        // before或after：parentCid为目标节点的父id
        pCid = dropNode.parent.data.catId === undefined ? 0 : dropNode.parent.data.catId
        // 节点顺序为目标父节点的子节点顺序
        siblings = dropNode.parent.childNodes
      } else {
        // inner：parentCid为目标节点的id
        pCid = dropNode.data.catId
        // 节点顺序为目标节点的子节点顺序
        siblings = dropNode.childNodes
      }
      this.pCid.push(pCid)

      // 当前拖拽节点的最新顺序
      for (let i = 0; i < siblings.length; i++) {
        // 如果处理的是当前正在拖拽的节点，需要修改parentCid
        if (siblings[i].data.catId === draggingNode.data.catId) {
          let catLevel = draggingNode.level
          // 当前节点的层级发生变化，需要修改层级以及该节点的子节点层级
          if (siblings[i].level !== draggingNode.level) {
            catLevel = siblings[i].level
            // 修改他子节点的层级
            this.updateChildNodeLevel(siblings[i])
          }
          this.updateNodes.push(
            {
              catId: siblings[i].data.catId,
              sort: i,
              parentCid: pCid,
              catLevel: catLevel
            }
          )
        } else {
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i })
        }
      }
      // 3 当前拖拽节点的最新层级
      console.log('updateNodes', this.updateNodes)
    },
    // 子节点层级递归修改
    updateChildNodeLevel (node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          const cNode = node.childNodes[i].data
          this.updateNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level
          })
          this.updateChildNodeLevel(node.childNodes[i])
        }
      }
    },
    // 判断是否能够拖动节点==>最终拖动结果的总层级不能大于3，否则不允许拖动
    allowDrop (draggingNode, dropNode, type) {
      // 被拖动的当前节点以及所在的父节点总层数不能大于3
      // console.log('allowDrop:', draggingNode, dropNode, type)
       // var level = this.countNodeLevel(draggingNode)
      // 判断被拖动的当前节点最深层级，如果没有子节点，则最深层级为本身，否则通过递归计算最深层级
      if (draggingNode.childNodes != null && draggingNode.childNodes.length > 0) {
        this.countNodeLevel(draggingNode)
      } else {
        this.maxLevel = draggingNode.level
      }
      // console.log(this.maxLevel, draggingNode.level)
      // 当前正在拖动的节点+父节点所在的深度不大于3即可
      let deep = Math.abs(this.maxLevel - draggingNode.level) + 1
      // console.log('该节点深度:', deep)
      // this.maxLevel
      if (type === 'inner') {
        return deep + dropNode.level <= 3
      } else {
        return deep + dropNode.parent.level <= 3
      }
    },
    // 统计当前节点的总层数，该节点的最深层级，并将其赋值给this.maxLevel
    countNodeLevel (node) {
      // 找到所有子节点，求出最大深度
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level
          }
          this.countNodeLevel(node.childNodes)
        }
      }
    },

    // 批量删除
    batchDelete () {
      let catIds = []
      let checkedNodes = this.$refs.categoryTree.getCheckedNodes()
      console.log('被选中的元素', checkedNodes)
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId)
      }

      this.$confirm(`是否批量删除【${catIds}】菜单?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'delete',
          data: this.$http.adornData(catIds, false)
        }).then(({ data }) => {
          this.$message.success('菜单批量删除成功!')
          // 刷新出新的菜单
          this.getMenus()
        }).catch(() => {})
      }).catch(() => {})
    }
  },
  created () {
    this.getMenus()
  }
}
</script>

<style scoped>

</style>
