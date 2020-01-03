import React, { Fragment } from 'react';
import { connect } from 'dva';
import { Drawer, Form, message, Button, Tree, Table } from 'antd';
import moment from 'moment';
import styles from '../../index.less';
import DiffList from './syncConfig/DiffList';

const FormItem = Form.Item;
const { TreeNode } = Tree;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};


class SyncConfig extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      step: 1,
      selectedRowKeys: [],
      selectItems: [],
      checkList: [],
      syncToNamespaces: [],
      syncItems: []
    };
  }
  componentDidMount() {
    this.onFetchNameSpaceListWithApp();

  }
  componentDidUpdate(prevProps, prevState) {
    const { nameSpaceListWithApp } = this.props;
    if (nameSpaceListWithApp !== prevProps.nameSpaceListWithApp) {
      this.onSetDefaultCheckList(nameSpaceListWithApp)
    }
  }

  //获取默认全部集群Id
  onSetDefaultCheckList = (list) => {
    let checkList = [];
    list.map((vo) => (
      vo.namespaceListResps.map((item) => {
        checkList.push(item.id.toString())
      })
    ))
    this.setState({
      checkList
    })
  }
  onFetchNameSpaceListWithApp = () => {
    const { dispatch, appDetail, info } = this.props;
    let baseInfo = info.baseInfo || {};
    dispatch({
      type: 'project/nameSpaceListWithApp',
      payload: {
        appCode: baseInfo.appCode,
        namespace: baseInfo.namespaceName
      }
    })
  }

  onSubmit = () => {

  }
  onGetAppEnvClusterNamespaceIds = () => {
    const { checkList } = this.state;
    let list = checkList, ids = [];
    list.map((vo) => {
      if (vo.indexOf('-') > -1) {
        return
      }
      ids.push({
        appEnvClusterId: vo
      })
    })
    return ids
  }
  onGetSyncItems = () => {
    const { selectItems } = this.state;
    let items = selectItems, newItems = [];
    items.map((vo) => {
      newItems.push(vo.item)
    })
    return newItems
  }
  onSelectCluster = (keys, e) => {
    this.setState({
      checkList: keys
    })
  }
  onSelectChange = (selectedRowKeys, selectItems) => {
    this.setState({
      selectedRowKeys,
      selectItems
    })
  }
  onNext = () => {
    const { selectedRowKeys } = this.state;
    if(!selectedRowKeys.length) {
      message.error('请选择要同步的配置')
      return
    }
    let syncToNamespaces = this.onGetAppEnvClusterNamespaceIds() || [];
    let syncItems = this.onGetSyncItems() || [];
    this.setState({
      step: 2,
      syncToNamespaces,
      syncItems
    })

  }
  onPrev = () => {
    this.setState({
      step: 1
    })
  }
  renderEnv() {
    const { envList, nameSpaceListWithApp, info } = this.props;
    let baseInfo = info.baseInfo || {}
    return (
      <FormItem label="同步集群">
        {
          nameSpaceListWithApp.length ?
            <Tree
              checkable
              defaultExpandAll
              onCheck={this.onSelectCluster}
              defaultCheckedKeys={['0-']}
            >
              <TreeNode title="全选" key={'0-'}>
                {
                  nameSpaceListWithApp.map((item, i) => (
                    <TreeNode title={item.env} key={`${item.env}-`}>
                      {
                        item.namespaceListResps.map((vo) => (
                          <TreeNode title={vo.name} key={vo.id.toString()} />
                        ))
                      }
                    </TreeNode>
                  ))
                }
              </TreeNode>
            </Tree> : null
        }
      </FormItem>
    )
  }
  renderTable(list) {
    const { selectedRowKeys } = this.state;
    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
    };
    const columns = [
      {
        title: 'Key',
        dataIndex: 'item.key',
        width: '20%',
      },
      {
        title: 'Value',
        dataIndex: 'item.value',
        width: '20%',
      },
      {
        title: '创建时间',
        dataIndex: 'item.createTime',
        render: (text, record) => (
          <span>{text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''}</span>
        )
      },
      {
        title: '修改时间',
        dataIndex: 'item.updateTime',
        render: (text, record) => (
          <span>{text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''}</span>
        )
      },
    ];
    return (
      <FormItem label="选择配置" style={{ marginBottom: 100 }}>
        <Table
          columns={columns}
          dataSource={list || []}
          rowSelection={rowSelection}
          bordered
          // onChange={this.onTableChange}
          pagination={false}
          rowKey={record => {
            return record.item.id;
          }}
        />
      </FormItem>
    )
  }
  renderStep1() {
    const { info } = this.props;
    let list = info.items || [];
    return (
      <Form {...formItemLayout}>
        {this.renderEnv()}
        {this.renderTable(list)}
      </Form>
    )
  }
  renderFooter() {
    const { step } = this.state;
    return (
      <div className={styles.drawerBottom}>
        {
          step === 1 && <Button type="primart" onClick={this.onNext}>下一步</Button>
        }
        {
          step === 2 &&
          <div>
            <Button onClick={this.onPrev} style={{ marginRight: 15 }}>上一步</Button>
            <Button type="primary">同步</Button>
          </div>
        }
      </div>
    )
  }
  render() {
    const { onCancel } = this.props;
    const { loading, step, syncItems, syncToNamespaces } = this.state;
    return (
      <Drawer
        title={`同步配置`}
        visible={true}
        onClose={onCancel}
        width={800}
      // footer={this.renderFooter()}
      >
        {
          step === 1 && this.renderStep1()
        }
        {
          step === 2 && <DiffList syncItems={syncItems} syncToNamespaces={syncToNamespaces} />
        }
        {this.renderFooter()}
      </Drawer>
    )
  }
}
export default Form.create()(connect(({ project }) => ({
  nameSpaceListWithApp: project.nameSpaceListWithApp,
  currentEnv: project.currentEnv
}))(SyncConfig));